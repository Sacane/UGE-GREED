package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.*;
import fr.ramatellier.greed.server.util.ComputeCommandParser;
import fr.ramatellier.greed.server.util.LogoutInformation;
import fr.ramatellier.greed.server.util.RouteTable;
import fr.ramatellier.greed.server.util.TramKind;
import fr.ramatellier.greed.server.util.file.ResponseToFileBuilder;
import fr.ramatellier.greed.server.util.file.ResultFormatHandler;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
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
    // Others
    private enum ServerState{
        ON_GOING, SHUTDOWN, STOPPED
    }
    private enum Command{
        INFO, STOP, SHUTDOWN, COMPUTE
    }
    private record CommandArgs(Command command, String[] args) {}

    private Server(int port) throws IOException {
        address = new InetSocketAddress(port);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = null;
        parentSocketAddress = null;
        selector = Selector.open();
        this.isRoot = true;
    }

    private Server(int hostPort, String IP, int connectPort) throws IOException {
        address = new InetSocketAddress(hostPort);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = SocketChannel.open();
        parentSocketAddress = new InetSocketAddress(IP, connectPort);
        selector = Selector.open();
        this.isRoot = false;
    }

    public void addRoom(ComputationEntity computationEntity) {
        Objects.requireNonNull(computationEntity);
        computationRoomHandler.add(computationEntity);
    }

    public ComputationEntity retrieveWaitingComputation(ComputationIdentifier idContext) {
        var response = computationRoomHandler.findById(idContext);
        return response.orElse(null);
    }

    public void incrementWaitingWorker(ComputationIdentifier id) {
        computationRoomHandler.increment(id);
    }

    public void incrementComputation(ComputationIdentifier id) {
        computationRoomHandler.incrementComputation(id);
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
        sendCommand(new CommandArgs(Command.COMPUTE, args));
    }

    /**
     * Example for compute :
     * COMPUTE C:/Users/johan/Documents/dev_project/SlowChecker.jar fr.uge.slow.SlowChecker 10 20
     * COMPUTE http://www-igm.univ-mlv.fr/~carayol/Factorizer.jar fr.uge.factors.Factorizer 10 20
     * COMPUTE http://www-igm.univ-mlv.fr/~carayol/SlowChecker.jar fr.uge.slow.SlowChecker 10 20
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
                        case "COMPUTE", "START" -> sendComputeCommand(line);
                        default -> System.out.println("Unknown command");
                    }
                }
            }
        } catch (InterruptedException e){
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
        return parentSocketAddress;
    }

    public boolean isRunning() {
        return state == ServerState.ON_GOING;
    }

    public boolean isShutdown() {
        return state == ServerState.SHUTDOWN;
    }

    public boolean isComputing() {
        return computationRoomHandler.isComputing();
    }

    public void addRoot(InetSocketAddress src, InetSocketAddress dst, ServerApplicationContext context) {
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

    /**
     * Launch a root Server on the given port.
     * @param port port of the server
     * @throws IOException if an I/O error occurs
     */
    public static void launchRoot(int port) throws IOException {
        new Server(port).launch();
    }

    public Optional<ComputationEntity> findComputationById(ComputationIdentifier id) {
        return computationRoomHandler.findById(id);
    }

    /**
     * Launch a server on the given hostPort, connected to another server.
     * @param hostPort port of the current server
     * @param IP IP of the server to connect to
     * @param connectPort port of the server to connect to
     * @throws IOException if an I/O error occurs
     */
    public static void launchConnected(int hostPort, String IP, int connectPort) throws IOException {
        Objects.requireNonNull(IP, "IP can't be null");
        new Server(hostPort, IP, connectPort).connect();
    }

    private void printInfo() {
        var root = isRoot ? "ROOT" : "CONNECTED";
        System.out.print("This server is a " + root + " server ");
        if(!isRoot){
            System.out.println("connected to " + parentSocketAddress);
        }
        System.out.println("This server is listening on " + address);
        System.out.println("Neighbours : ");
        routeTable.onNeighboursDo(null, info -> System.out.println("- " + info.address()));
        System.out.println("Route table content: \n" + routeTable);
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
                    if (isRoot) {
                        logger.warning("You can't shutdown a root server manually");
                        continue;
                    }
                    logger.info("Command SHUTDOWN received");
                    state = ServerState.SHUTDOWN;
                    if((logoutInformation == null || logoutInformation.allConnected()) && !isComputing()) {
                        sendLogout();
                    }
                }
                case COMPUTE -> {
                    logger.info("Command COMPUTE received");
                    parseAndCompute(command.args());
                }
            }
        }
    }

    private void parseAndCompute(String[] args) {
        var line = Arrays.stream(args).reduce("", (s, s2) -> s + " " + s2);
        var parser = new ComputeCommandParser(line.trim());
        if(!parser.check()){
            System.out.println("The computation command is not valid");
            return;
        }
        processComputeCommand(parser.get());
    }

    private void processComputeCommand(ComputeInfo info) {
        var id = new ComputationIdentifier(computationIdentifierValue++, address);
        var entity = new ComputationEntity(id, info);
        if(routeTable.neighbors().size() == 0) {
            var response = Client.checkerFromHTTP(info.url(), info.className());
            if(response.isEmpty()) {
                logger.severe("FAILED TO GET CHECKER");
                return ;
            }
            var checker = response.get();
            var fileBuilder = new ResponseToFileBuilder("result" + computationIdentifierValue + ".txt");
            try {
                for(var i = info.start(); i < info.end(); i++) {
                    fileBuilder.append(checker.check(i));
                }
                fileBuilder.build();
            } catch (InterruptedException e) {
                logger.severe("CHECKER INTERRUPTED");
            } catch (IOException e) {
                logger.severe("FAILED TO WRITE FILE");
            }
        }
        else {
            computationRoomHandler.prepare(entity, routeTable.size());
            routeTable.performOnAllAddress(address -> transfer(address.address(), new WorkRequestPacket(
                    new IDPacket(this.address), new IDPacket(address.address()),
                    new LongPacketPart(id.id()),
                    new CheckerPacket(info.url(), info.className()),
                    new RangePacket(info.start(), info.end()),
                    new LongPacketPart(info.end() - info.start())
            )));
        }
    }

    public void treatComputationResult(ComputationIdentifier id, String result) throws IOException {
        computationRoomHandler.incrementUc(id);
        resultFormatHandler.append(id, result);
        if(computationRoomHandler.isComputationDone(id)){
            resultFormatHandler.build(id);
        }
    }

    public void connectToNewParent(IDPacket packet) throws IOException {
        var oldParentAddress = parentSocketAddress;
        var ancestors = routeTable.ancestors(parentSocketAddress);
        parentSocketChannel = SocketChannel.open();
        parentSocketAddress = new InetSocketAddress(packet.getHostname(), packet.getPort());
        logger.info("Trying to connect to " + parentSocketAddress + " ...");
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new ServerApplicationContext(this, parentKey);
        context.queuePacket(new ReconnectPacket(new IDPacket(address), new IDPacketList(ancestors.stream().map(IDPacket::new).collect(Collectors.toList()))));
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
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new ServerApplicationContext(this, parentKey);
        context.queuePacket(new ConnectPacket(new IDPacket(address)));
        parentKey.interestOps(SelectionKey.OP_CONNECT);
        parentKey.attach(context);
        initConnection();
    }

    private void launch() throws IOException {
        if(!isRoot) {
            throw new IllegalStateException("This server is not a root server");
        }
        serverSocketChannel.configureBlocking(false);
        serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Server started on " + address);
        initConnection();
    }

    public void wakeup() {
        selector.wakeup();
    }

    private void initConnection() throws IOException {
        System.out.println("Enter a command");
        Thread.ofPlatform()
                .daemon()
                .start(this::consoleRun);
        while (!Thread.interrupted()) {
            try {
                System.out.println("Waiting for a connection");
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
            if (key.isValid() && key.equals(parentKey) && key.isConnectable()) {
                doConnect(key);
            }
            if (key.isValid() && key.equals(serverKey) && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((ServerApplicationContext) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((ServerApplicationContext) key.attachment()).doRead();
            }
        } catch (IOException e) {
//            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
        }
    }

    private void doConnect(SelectionKey key) throws IOException {
        if (!parentSocketChannel.finishConnect()) {
            return ;
        }

        key.interestOps(SelectionKey.OP_WRITE);
        serverSocketChannel.configureBlocking(false);
        serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
    public void broadcast(FullPacket packet, InetSocketAddress src) {
        Objects.requireNonNull(packet);
        Objects.requireNonNull(src);
        routeTable.onNeighboursDo(src, addressContext -> addressContext.context().queuePacket(packet));
    }

    /**
     * transfer the packet to the destination.
     * @param dst destination
     * @param packet packet to transfer
     */
    public void transfer(InetSocketAddress dst, FullPacket packet) {
        if(packet.kind() != TramKind.TRANSFER){
            throw new AssertionError("Only transfer packet can be transferred");
        }
        if(dst.equals(address)){
            return;
        }
        routeTable.sendTo(dst, packet);
    }

    public void sendLogout() {
        routeTable.sendTo(parentSocketAddress, new LogoutRequestPacket(new IDPacket(address),
                new IDPacketList(routeTable.neighbors().stream().filter(n -> !n.equals(parentSocketAddress)).map(IDPacket::new).toList())
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
        } catch (IOException ignored) {
        }
    }

    public List<ServerApplicationContext> daughtersContext() {
        return routeTable.daughtersContext(parentSocketAddress);
    }

    public void updateRoom(ComputationIdentifier id, long start, long end) {
        computationRoomHandler.updateRange(id, start, end);
    }
}
