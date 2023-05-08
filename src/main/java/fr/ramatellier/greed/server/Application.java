package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.context.ClientApplicationContext;
import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.context.ServerApplicationContext;
import fr.ramatellier.greed.server.frame.component.*;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.util.ComputeCommandParser;
import fr.ramatellier.greed.server.frame.FrameKind;
import fr.ramatellier.greed.server.util.LogoutInformation;
import fr.ramatellier.greed.server.util.RouteTable;
import fr.ramatellier.greed.server.compute.ResultFormatter;
import fr.ramatellier.greed.server.util.http.NonBlockingHTTPJarProvider;
import fr.uge.ugegreed.Checker;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.channels.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Application {
    private static final Logger logger = Logger.getLogger(Application.class.getName());
    // Self server field
    private final ServerSocketChannel serverSocketChannel;
    private SelectionKey serverKey;
    private final Selector selector;
    private final boolean isRoot;
    private final InetSocketAddress address;
    private final RouteTable routeTable = new RouteTable();
    private ServerState state = ServerState.ON_GOING;
    private final ArrayBlockingQueue<CommandArgs> commandQueue = new ArrayBlockingQueue<>(10);
    private static long computationIdentifierValue;
    private final AtomicLong currentOnWorkingComputations = new AtomicLong(0);
    private final SocketCandidate socketCandidate = new SocketCandidate();
    private final ComputationRoomHandler computationRoomHandler = new ComputationRoomHandler();
    public static final long MAXIMUM_COMPUTATION = 1_000_000_000;
    private LogoutInformation logoutInformation;
    private final ResultFormatter resultFormatHandler = new ResultFormatter();
    // Parent information
    private SocketChannel parentSocketChannel;
    private InetSocketAddress parentSocketAddress;
    private SelectionKey parentKey;
    private final ThreadComputationHandler computation = new ThreadComputationHandler(100);
    private final LinkedBlockingQueue<SendInformation> packets = new LinkedBlockingQueue<>();
    // Others
    private enum ServerState {
        ON_GOING, SHUTDOWN, STOPPED
    }
    private enum Command {
        INFO, STOP, SHUTDOWN, START
    }
    private record CommandArgs(Command command, String[] args) {}
    private record SendInformation(InetSocketAddress address, Frame packet) {}

    private Application(InetSocketAddress selfAddress, InetSocketAddress remoteAddress) throws IOException {
        address = selfAddress;
        parentSocketAddress = remoteAddress;
        if(parentSocketAddress != null) {
            parentSocketChannel = SocketChannel.open();
            this.isRoot = false;
        } else {
            this.isRoot = true;
        }
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        selector = Selector.open();
        sendResponseThread();
    }

    private Application(InetSocketAddress selfAddress) throws IOException {
        this(selfAddress, null);
        sendResponseThread();
    }

    /**
     * This method add a room to the computation handler
     *
     * @param computationEntity The computation we want to create
     */
    public void addRoom(ComputationEntity computationEntity) {
        Objects.requireNonNull(computationEntity);
        computationRoomHandler.add(computationEntity);
    }

    /**
     * This method add a task for the thread computation
     *
     * @param taskComputation The task we want to be done
     */
    public void addTask(TaskComputation taskComputation) {
        Objects.requireNonNull(taskComputation);
        try {
            computation.putTask(taskComputation);
        } catch (InterruptedException e) {
            // Ignore exception
        }
    }

    /**
     * This method gets a computation
     *
     * @param idContext The id of the computation we want to get
     * @return The computation with the given id
     */
    public ComputationEntity retrieveWaitingComputation(ComputationIdentifier idContext) {
        Objects.requireNonNull(idContext);
        var response = computationRoomHandler.findById(idContext);
        return response.orElse(null);
    }

    /**
     * This method increment the number of computation done for an id
     *
     * @param id The id of the computation we want to increment
     */
    public void incrementWaitingWorker(ComputationIdentifier id) {
        computationRoomHandler.increment(id);
    }

    /**
     * This method store a socket identifier for a computation identifier
     *
     * @param id The computation identifier where we want to store
     * @param ucId The socket identifier that we want to be associate with id
     */
    public void storeComputation(ComputationIdentifier id, SocketUcIdentifier ucId) {
        socketCandidate.store(id, ucId);
    }

    /**
     * This method if a room is ready
     *
     * @param id The id of the computation that we want to check
     * @return true if the room is ready else false
     */
    public boolean isRoomReady(ComputationIdentifier id){
        return computationRoomHandler.isReady(id);
    }

    /**
     * This method will get the socket identifier associate with an id
     *
     * @param id The id of the computation
     * @return The list of socket identifier that are associated with this id
     */
    public List<SocketUcIdentifier> availableSocketsUc(ComputationIdentifier id){
        return socketCandidate.availableSockets(id);
    }

    /**
     * This method update the parent address
     *
     * @param address The new InetSocketAddress of the parent
     */
    public void updateParentAddress(InetSocketAddress address) {
        parentSocketAddress = address;
    }

    /**
     * This method delete an address from the route table
     *
     * @param address The InetSocketAddress we want to delete
     */
    public void deleteAddress(InetSocketAddress address) {
        routeTable.delete(address);
    }

    /**
     * This method update the logout information
     *
     * @param address The InetSocketAddress that want to logout
     * @param daughters The list of the daughters
     */
    public void newLogoutRequest(InetSocketAddress address, List<InetSocketAddress> daughters) {
        logoutInformation = new LogoutInformation(address, daughters);
    }

    /**
     * This method is used when you receive a reconnect request
     *
     * @param address The InetSocketAddress that want to reconnect
     */
    public void receiveReconnect(InetSocketAddress address) {
        logoutInformation.add(address);
    }

    /**
     * This method check if all the daughters are connected
     *
     * @return true if all the daughters are connected else false
     */
    public boolean allConnected() {
        return logoutInformation.allConnected();
    }

    /**
     * This method get the address of the server that wants to shut down
     *
     * @return The InetSocketAddress of the server that want to shut down
     */
    public InetSocketAddress getAddressLogout() {
        return logoutInformation.getAddress();
    }

    private void sendCommand(CommandArgs command) throws InterruptedException {
        commandQueue.put(command);
        selector.wakeup();
    }

    private void sendComputeCommand(String line) throws InterruptedException {
        if(line.split(" ").length != 5){
            logger.warning("Invalid given command : " + line);
            return;
        }
        var args = Arrays.stream(line.split(" ")).skip(1).toArray(String[]::new);
        sendCommand(new CommandArgs(Command.START, args));
    }

    /**
     * //TODO Enlever ce commentaire
     * Example for compute :
     * START http://www-igm.univ-mlv.fr/~carayol/Factorizer.jar fr.uge.factors.Factorizer 10 20
     * START http://www-igm.univ-mlv.fr/~carayol/SlowChecker.jar fr.uge.slow.SlowChecker 10 20
     * START http://www-igm.univ-mlv.fr/~carayol/Collatz.jar fr.uge.collatz.Collatz 0 2
     */
    private void consoleRun() {
        try {
            try(var scan = new Scanner(System.in)) {
                while(scan.hasNextLine()) {
                    var line  = scan.nextLine();
                    var command = line.split(" ")[0];
                    switch(command) {
                        case "INFO" -> sendCommand(new CommandArgs(Command.INFO, null));
                        case "STOP" -> sendCommand(new CommandArgs(Command.STOP, null));
                        case "SHUTDOWN" -> sendCommand(new CommandArgs(Command.SHUTDOWN, null));
                        case "START" -> sendComputeCommand(line);
                        default -> logger.severe("Unknown command");
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.info("Console Thread has been interrupted");
        } finally {
            logger.info("Console Thread has been stopped");
        }
    }

    /**
     * This method get the actual number of computation we are working on
     *
     * @return The number of computation we are working on
     */
    public long currentOnWorkingComputationsValue() {
        return currentOnWorkingComputations.get();
    }

    /**
     * This method get the server address
     *
     * @return the inetSocketAddress of the server.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * This method get the parent address
     *
     * @return The InetSocketAddress of the parent
     */
    public InetSocketAddress getParentSocketAddress() {
        if(isRoot) throw new IllegalStateException("Root server has no parent");
        return parentSocketAddress;
    }

    /**
     * This method check if the server is running
     *
     * @return true if the server is running else false
     */
    public boolean isRunning() {
        return state == ServerState.ON_GOING;
    }

    /**
     * This method check if the server is shut down
     *
     * @return true if the server is shut down else false
     */
    public boolean isShutdown() {
        return state == ServerState.SHUTDOWN;
    }

    private boolean hasFinishedComputing() {
        return !computationRoomHandler.isComputing();
    }

    /**
     * This method update the route table of the server
     *
     * @param src The InetSocketAddress of the src
     * @param dst The InetSocketAddress of the dst
     * @param context The context associate with the dst
     */
    public void updateRouteTable(InetSocketAddress src, InetSocketAddress dst, Context context) {
        if(!src.equals(address)) {
            logger.info("Root table has been updated");
            routeTable.putOrUpdate(src, dst, context);
        }
    }

    /**
     * This method get all the addresses
     *
     * @return The set of registered addresses in the route table
     */
    public Set<InetSocketAddress> registeredAddresses() {
        return routeTable.registeredAddresses();
    }

    /**
     * This method will get a computation with his id
     *
     * @param id The id of the computation
     * @return The computation as an Optional
     */
    public Optional<ComputationEntity> findComputationById(ComputationIdentifier id) {
        Objects.requireNonNull(id);
        return computationRoomHandler.findById(id);
    }

    private void printInfo() {
        System.out.println("====================APPLICATION INFO==================================");
        var root = isRoot ? "ROOT" : "CONNECTED";
        System.out.print("This server is a " + root + " server ");
        if(!isRoot){
            System.out.println("connected to " + parentSocketAddress);
        }
        System.out.println("This server is listening on " + address);
        System.out.println("Neighbours : ");
        routeTable.onNeighboursDo(null, info -> System.out.println("- " + info.address()));
        System.out.println("Route table content: \n" + routeTable);
        System.out.println("=======================================================================");
    }

    private void processCommand() {
        for(;;) {
            var command = commandQueue.poll();
            if (command == null) {
                return;
            }
            switch (command.command()) {
                case INFO -> printInfo();
                case STOP -> {
                    logger.info("Command STOP received");
                    state = (state == ServerState.ON_GOING) ? ServerState.STOPPED : ServerState.ON_GOING;
                }
                case SHUTDOWN -> {
                    logger.info("Command SHUTDOWN received");
                    if (isRoot) {
                        if (routeTable.neighbors().size() == 0) {
                            shutdown();
                        }
                        else {
                            logger.warning("You can't shutdown a root server manually, you must stop all the connected servers first");
                        }
                    }
                    else {
                        state = ServerState.SHUTDOWN;
                        if((logoutInformation == null || logoutInformation.allConnected()) && hasFinishedComputing()) {
                            sendLogout();
                        }
                    }
                }
                case START -> {
                    logger.info("Command START received");
                    parseAndCompute(command.args());
                }
            }
        }
    }

    /**
     * This method create an Application as a ROOT
     *
     * @param port The port when we host
     * @return The new Application
     * @throws IOException If we can't create the Application
     */
    public static Application root(int port) throws IOException {
        checkPort(port);
        var address = new InetSocketAddress(port);
        return new Application(address);
    }

    /**
     * This method create an Application as a CONNECTED
     *
     * @param selfPort The port when we host
     * @param remoteIp The ip we will connect to
     * @param remotePort The port we will connect to
     * @return The new Application
     * @throws IOException If we can't create the Application
     */
    public static Application child(int selfPort, String remoteIp, int remotePort) throws IOException {
        checkPort(selfPort, remotePort);
        var selfAddress = new InetSocketAddress(selfPort);
        var remoteAddress = new InetSocketAddress(remoteIp, remotePort);
        return new Application(selfAddress, remoteAddress);
    }
    private static void checkPort(int... ports){
        for(var port : ports){
            if(port < 0 || port > 65535){
                throw new IllegalArgumentException("Port must be between 0 and 65535");
            }
        }
    }

    private void parseAndCompute(String[] args) {
        var line = Arrays.stream(args).reduce("", (s, s2) -> s + " " + s2);
        var parser = new ComputeCommandParser(line.trim());
        if(!parser.check()){
            logger.severe("The computation command is not valid");
            return;
        }
        processComputeCommand(parser.get());
    }

    private void processComputeCommand(ComputeInfo info) {
        var id = new ComputationIdentifier(computationIdentifierValue++, address);
        var entity = new ComputationEntity(id, info);
        if(routeTable.neighbors().size() == 0) {
            try {
                System.out.println(entity.info().url());
                computationRoomHandler.prepare(entity, routeTable.size());
                var httpClient = NonBlockingHTTPJarProvider.fromURL(new URL(entity.info().url()));
                httpClient.onDone(body -> {
                    var checker = retrieveChecker(httpClient, entity.info().className());
                    for(var i = info.start(); i < info.end(); i++) {
                        addTask(new TaskComputation(new WorkAssignmentFrame(null, null, LongComponent.of(id.id()), null), checker, entity.id(), i));
                    }
                });
                httpClient.launch();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                logger.severe("CANNOT GET THE CHECKER");
            }
        }
        else {
            computationRoomHandler.prepare(entity, routeTable.size());
            routeTable.performOnAllAddress(address -> transfer(address.address(), new WorkRequestFrame(
                    new IDComponent(this.address), new IDComponent(address.address()),
                    LongComponent.of(id.id()),
                    new CheckerComponent(info.url(), info.className()),
                    new RangeComponent(info.start(), info.end()),
                    LongComponent.of(info.end() - info.start())
            )));
        }
    }

    /**
     * This method get a checker
     *
     * @param provider The jar we use to get the checker
     * @param className The class name where the checker is
     * @return The Checker if it exists else null
     */
    public static Checker retrieveChecker(NonBlockingHTTPJarProvider provider, String className){
        var path = Path.of(provider.getFilePath());
        System.out.println(path);
        var checkerResult = Client.checkerFromDisk(path, className);
        if(checkerResult.isEmpty()) {
            logger.severe("CANNOT GET THE CHECKER");
            return null;
        }
        return checkerResult.get();
    }

    /**
     * This method will treat a result of a computation
     *
     * @param id The id of the computation
     * @param result The result that we get from the Checker
     * @throws IOException If the build of the file failed
     */
    public void treatComputationResult(ComputationIdentifier id, String result) throws IOException {
        computationRoomHandler.incrementUc(id);
        resultFormatHandler.append(id, result);
        if(computationRoomHandler.isComputationDone(id)){
            resultFormatHandler.build(id);
        }
    }

    /**
     * This method connect the application to a new application
     *
     * @param packet The packet that get the information for the connection
     * @throws IOException If the connection failed
     */
    public void connectToNewParent(IDComponent packet) throws IOException {
        var oldParentAddress = parentSocketAddress;
        var ancestors = routeTable.ancestors(parentSocketAddress);
        parentSocketChannel = SocketChannel.open();
        parentSocketAddress = new InetSocketAddress(packet.getHostname(), packet.getPort());
        logger.info("Trying to connect to " + parentSocketAddress + " ...");
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new ClientApplicationContext(this, parentKey);
        context.queuePacket(new ReconnectFrame(new IDComponent(address), new IDListComponent(ancestors.stream().map(IDComponent::new).collect(Collectors.toList()))));
        parentKey.interestOps(SelectionKey.OP_CONNECT);
        parentKey.attach(context);
        deleteAddress(oldParentAddress);
        routeTable.updateToContext(oldParentAddress, parentSocketAddress, context);
    }

    private void connect() throws IOException {
        if(isRoot || parentSocketAddress == null) {
            throw new IllegalStateException("This server is a root server");
        }
        logger.info("Trying to connect to " + parentSocketAddress + " ...");
        serverSocketChannel.configureBlocking(false);
        serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new ClientApplicationContext(this, parentKey);
        context.queuePacket(new ConnectFrame(new IDComponent(address)));
        parentKey.interestOps(SelectionKey.OP_CONNECT);
        parentKey.attach(context);
        initConnection();
    }

    /**
     * This method launch an application in ROOT or CONNECTED
     *
     * @throws IOException If the launch have failed
     */
    public void launch() throws IOException {
        if(isRoot){
            start();
        } else {
            connect();
        }
    }

    private void start() throws IOException {
        if(!isRoot) {
            throw new IllegalStateException("This server is not a root server");
        }
        serverSocketChannel.configureBlocking(false);
        serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Server started on " + address);
        initConnection();
    }

    private void processPacket() {
        while(!packets.isEmpty()) {
            var packet = packets.poll();
            routeTable.sendTo(packet.address(), packet.packet());
        }
    }

    private void initConnection() throws IOException {
        System.out.println("Enter a command");
        Thread.ofPlatform()
                .daemon()
                .start(this::consoleRun);

        while (!Thread.interrupted()) {
            try {
                processPacket();
                selector.select(this::treatKey);
                processCommand();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            } catch (CancelledKeyException exception) {
                return ;
            }
        }
    }

    private void treatKey(SelectionKey key) {
        try {
            if (key.isValid() && key.isConnectable()) {
                ((ClientApplicationContext) key.attachment()).doConnect();
            }
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
                // ((ServerApplicationContext) key.attachment()).doAccept(key, selector, this);

            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
//            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        logger.info("Accepting connection...");
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        if (sc == null) {
            return;
        }
        sc.configureBlocking(false);
        var socketKey = sc.register(selector, SelectionKey.OP_READ);
        socketKey.attach(new ServerApplicationContext(this, socketKey));
        System.out.println("Connection accepted from " + sc.getRemoteAddress());
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = key.channel();
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    /**
     * Broadcast a packet to all neighbours except the source.
     *
     * @param packet the packet to broadcast
     * @param src the source of the packet (the packet won't be sent to this address)
     */
    public void broadcast(Frame packet, InetSocketAddress src) {
        Objects.requireNonNull(packet);
        Objects.requireNonNull(src);
        System.out.println("Broadcasting packet " + packet + " from " + src);
        routeTable.onNeighboursDo(src, addressContext -> addressContext.context().queuePacket(packet));
    }

    /**
     * transfer the packet to the destination.
     *
     * @param dst destination
     * @param packet packet to transfer, the packet must implement the {@link TransferFrame} kind
     */
    public void transfer(InetSocketAddress dst, TransferFrame packet) {
        if(packet.kind() != FrameKind.TRANSFER) {
            throw new AssertionError("Only transfer packet can be transferred");
        }
        if(dst.equals(address)) {
            return;
        }
        try {
            packets.put(new SendInformation(dst, packet));
        } catch (InterruptedException ignored) {
            silentlyClose(parentKey);
        }
    }

    /**
     * This method send a logout request to the parent
     */
    public void sendLogout() {
        routeTable.sendTo(parentSocketAddress, new LogoutRequestFrame(new IDComponent(address),
                new IDListComponent(routeTable.neighbors().stream().filter(n -> !n.equals(parentSocketAddress)).map(IDComponent::new).toList())
        ));
    }

    /**
     * Shutdown the current server and close all connections.
     */
    public void shutdown() {
        try {
            serverSocketChannel.close();
            silentlyClose(serverKey);
            if(!isRoot) silentlyClose(parentKey);
            state = ServerState.STOPPED;
            Thread.currentThread().interrupt();
        } catch (IOException ignored) {
        }
    }

    /**
     * This method get all the context of the daughters
     *
     * @return The list of the context of the daughters
     */
    public List<Context> daughtersContext() {
        return routeTable.daughtersContext(parentSocketAddress);
    }

    /**
     * This method update a room computation
     *
     * @param id The id of the computation
     * @param start The new start of the interval
     * @param end The new end of the interval
     */
    public void updateRoom(ComputationIdentifier id, long start, long end) {
        computationRoomHandler.updateRange(id, start, end);
    }

    private void sendResponseThread() {
        Thread.ofPlatform().daemon().start(() -> {
            for(;;) {
                try {
                    var response = computation.takeResponse();
                    if(response.packet().src() != null) {
                        transfer(response.packet().src().getSocket(), new WorkResponseFrame(
                                response.packet().dst(),
                                response.packet().src(),
                                response.packet().requestId(),
                                new ResponseComponent(response.value(), response.response(), response.code())
                        ));
                        computationRoomHandler.incrementComputation(response.id());
                    }
                    else {
                        var id = new ComputationIdentifier(response.packet().requestId().get(), getAddress());
                        treatComputationResult(id, response.response());
                    }
                    if(isShutdown() && hasFinishedComputing()) {
                        sendLogout();
                    }
                    selector.wakeup();
                } catch (InterruptedException e) {
                    return;
                } catch (IOException e) {
                    // Ignore exception
                    shutdown();
                }
            }
        });
    }
}
