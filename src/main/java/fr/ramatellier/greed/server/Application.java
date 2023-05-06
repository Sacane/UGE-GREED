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
import fr.ramatellier.greed.server.util.file.ResultFormatHandler;
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

//TODO quentin

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
    private final ResultFormatHandler resultFormatHandler = new ResultFormatHandler();
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

    private Application(int port) throws IOException {
        address = new InetSocketAddress(port);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = null;
        parentSocketAddress = null;
        selector = Selector.open();
        this.isRoot = true;
        sendResponseThread();
    }

    private Application(int hostPort, String IP, int connectPort) throws IOException {
        address = new InetSocketAddress(hostPort);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = SocketChannel.open();
        parentSocketAddress = new InetSocketAddress(IP, connectPort);
        selector = Selector.open();
        this.isRoot = false;
        sendResponseThread();
    }

    public void addRoom(ComputationEntity computationEntity) {
        Objects.requireNonNull(computationEntity);
        computationRoomHandler.add(computationEntity);
    }

    public void addTask(TaskComputation taskComputation) {
        try {
            computation.putTask(taskComputation);
        } catch (InterruptedException e) {
            // Ignore exception
        }
    }

    public ComputationEntity retrieveWaitingComputation(ComputationIdentifier idContext) {
        var response = computationRoomHandler.findById(idContext);
        return response.orElse(null);
    }

    public void incrementWaitingWorker(ComputationIdentifier id) {
        computationRoomHandler.increment(id);
    }

    public void storeComputation(ComputationIdentifier id, SocketUcIdentifier ucId){
        socketCandidate.store(id, ucId);
    }

    public boolean isRoomReady(ComputationIdentifier id){
        return computationRoomHandler.isReady(id);
    }

    public List<SocketUcIdentifier> availableSocketsUc(ComputationIdentifier id){
        return socketCandidate.availableSockets(id);
    }

    public void updateParentAddress(InetSocketAddress address) {
        parentSocketAddress = address;
    }

    public void deleteAddress(InetSocketAddress address) {
        routeTable.delete(address);
    }

    public void newLogoutRequest(InetSocketAddress address, List<InetSocketAddress> daughters) {
        logoutInformation = new LogoutInformation(address, daughters);
    }

    public void receiveReconnect(InetSocketAddress address) {
        logoutInformation.add(address);
    }

    public boolean allConnected() {
        return logoutInformation.allConnected();
    }

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
     * START C:/Users/johan/Documents/dev_project/SlowChecker.jar fr.uge.slow.SlowChecker 10 20
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
                        default -> System.out.println("Unknown command");
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.info("Console Thread has been interrupted");
        } finally {
            logger.info("Console Thread has been stopped");
        }
    }

    public long currentOnWorkingComputationsValue() {
        return currentOnWorkingComputations.get();
    }

    /**
     * @return the inetSocketAddress of the server.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    public InetSocketAddress getParentSocketAddress() {
        if(isRoot) throw new IllegalStateException("Root server has no parent");
        return parentSocketAddress;
    }

    public boolean isRunning() {
        return state == ServerState.ON_GOING;
    }

    public boolean isShutdown() {
        return state == ServerState.SHUTDOWN;
    }

    private boolean hasFinishedComputing() {
        return !computationRoomHandler.isComputing();
    }

    public void updateRouteTable(InetSocketAddress src, InetSocketAddress dst, Context context) {
        if(!src.equals(address)) {
            logger.info("Root table has been updated");
            routeTable.putOrUpdate(src, dst, context);
        }
    }

    /**
     * returns the set of registered addresses in the rootTable.
     */
    public Set<InetSocketAddress> registeredAddresses() {
        return routeTable.registeredAddresses();
    }

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

    void processCommand() {
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

    public static Application root(int port) throws IOException {
        return new Application(port);
    }
    public static Application child(int selfPort, String remoteIp, int remotePort) throws IOException {
        return new Application(selfPort, remoteIp, remotePort);
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

    public void treatComputationResult(ComputationIdentifier id, String result) throws IOException {
        computationRoomHandler.incrementUc(id);
        resultFormatHandler.append(id, result);
        if(computationRoomHandler.isComputationDone(id)){
            resultFormatHandler.build(id);
        }
    }

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

    public List<Context> daughtersContext() {
        return routeTable.daughtersContext(parentSocketAddress);
    }

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
