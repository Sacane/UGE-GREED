package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.compute.ComputeWorkHandler;
import fr.ramatellier.greed.server.packet.ConnectPacket;
import fr.ramatellier.greed.server.packet.FullPacket;
import fr.ramatellier.greed.server.packet.WorkRequestPacket;
import fr.ramatellier.greed.server.util.RootTable;
import fr.ramatellier.greed.server.util.TramKind;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    // Self server field
    private final ServerSocketChannel serverSocketChannel;
    private SelectionKey serverKey;
    private final Selector selector;
    private final boolean isRoot;
    private final InetSocketAddress address;
    private final RootTable rootTable = new RootTable();
    private ServerState state = ServerState.ON_GOING;
    private final ArrayBlockingQueue<CommandArgs> commandQueue = new ArrayBlockingQueue<>(10);
    private final ComputeWorkHandler handler;

    // Parent information
    private final SocketChannel parentSocketChannel;
    private final InetSocketAddress parentSocketAddress;
    private SelectionKey parentKey;


    // Others

    public static final Charset UTF8 = StandardCharsets.UTF_8;



    private enum Command{
        INFO, STOP, SHUTDOWN, COMPUTE
    }
    private record CommandArgs(Command command, String[] args) {}

    private enum ServerState{
        ON_GOING, STOPPED
    }

    private Server(int port) throws IOException {
        address = new InetSocketAddress(port);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = null;
        parentSocketAddress = null;
        selector = Selector.open();
        this.isRoot = true;
        this.handler = new ComputeWorkHandler(address.getHostName());
    }
    private Server(int hostPort, String IP, int connectPort) throws IOException {
        address = new InetSocketAddress(hostPort);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        parentSocketChannel = SocketChannel.open();
        parentSocketAddress = new InetSocketAddress(IP, connectPort);
        selector = Selector.open();
        this.isRoot = false;
        this.handler = new ComputeWorkHandler(address.getHostName());
    }

    private void sendCommand(CommandArgs command) throws InterruptedException {
        synchronized (commandQueue){
            commandQueue.put(command);
            selector.wakeup();
        }
    }
    private void sendComputeCommand(String line) throws InterruptedException {
        if(line.split(" ").length != 5){
            logger.warning("Invalid given command : " + line);
            System.out.println("Expected " + (5 - 1) + " arguments");
            return;
        }
        var args = Arrays.stream(line.split(" ")).skip(1).toArray(String[]::new);
        sendCommand(new CommandArgs(Command.COMPUTE, args));
    }

    private void consoleRun(){
        try{
            try(var scan = new Scanner(System.in)){
                while(scan.hasNextLine()){
                    var line  = scan.nextLine();
                    var command = line.split(" ")[0];
                    switch(command){
                        case "INFO" -> sendCommand(new CommandArgs(Command.INFO, null));
                        case "STOP" -> sendCommand(new CommandArgs(Command.STOP, null));
                        case "SHUTDOWN" -> sendCommand(new CommandArgs(Command.SHUTDOWN, null));
                        case "COMPUTE" -> sendComputeCommand(line);
                        default -> System.out.println("Unknown command");
                    }
                }
            }
        }catch (InterruptedException e){
            logger.info("Console Thread has been interrupted");
        } finally {
            logger.info("Console Thread has been stopped");
        }
    }

    /**
     * transfer the packet to the destination.
     * @param dst destination
     * @param packet packet to transfer
     */
    public void transfer(InetSocketAddress dst, FullPacket packet) {
        if(packet.kind() != TramKind.TRANSFERT){
            throw new AssertionError("Only transfer packet can be transferred");
        }
        if(dst.equals(address)){
            return;
        }
        rootTable.sendTo(dst, packet);
    }

    /**
     * @return the inetSocketAddress of the server.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    public boolean isRunning() {
        return state != ServerState.STOPPED;
    }

    public void addRoot(InetSocketAddress src, InetSocketAddress dst, Context context) {
        if(!src.equals(address)) {
            logger.info("Root table has been updated");
            rootTable.putOrUpdate(src, dst, context);
        }
    }

    ComputeWorkHandler getHandler() {
        return handler;
    }

    /**
     * returns the set of registered addresses in the rootTable.
     */
    public Set<InetSocketAddress> registeredAddresses() {
        return rootTable.registeredAddresses();
    }

    /**
     * Launch a root Server on the given port.
     * @param port port of the server
     * @throws IOException if an I/O error occurs
     */
    public static void launchRoot(int port) throws IOException {
        new Server(port).launch();
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
        } else {
            System.out.println();
        }
        System.out.println("Connected to " + address);
        System.out.println("Neighbours : ");
        rootTable.onNeighboursDo(null, info -> System.out.println("- " + info.address()));
        System.out.println("Root table : \n" + rootTable);
    }
    void processCommand(){
        for(;;){
            var command = commandQueue.poll();
            if(command == null){
                return;
            }
            switch(command.command()){
                case INFO -> {
                    printInfo();
                }
                case STOP -> {
                    logger.info("Command STOP received");
                    state = (state == ServerState.ON_GOING) ? ServerState.STOPPED : ServerState.ON_GOING;
                }
                case SHUTDOWN -> {
                    logger.info("Command SHUTDOWN received");
                }
                case COMPUTE -> {
                    logger.info("Command COMPUTE received");
                    parseComputeCommand(command.args());
                }
            }
        }
    }
    private void parseComputeCommand(String[] args) {
        var line = Arrays.stream(args).reduce("", (s, s2) -> s + " " + s2);
        var parser = new ComputeCommandParser(line.trim());
        if(!parser.check()){
            System.out.println("The computation command is not valid");
            return;
        }
        processComputeCommand(parser.get());
    }

    private void processComputeCommand(ComputeInfo info) {
        var workers = rootTable.allAddress(); //TODO remove this method -> access to all Address is not necessary

        for(var worker: workers) {
            var packet = new WorkRequestPacket(address, worker.address(), 0, info.url(), info.className(), info.start(), info.end(), 10000);
//            worker.context().queuePacket(packet);
            rootTable.sendTo(worker.address(), packet);
        }
    }

    private void connect() throws IOException {
        if(isRoot || parentSocketAddress == null) {
            throw new IllegalStateException("This server is a root server");
        }
        logger.info("Trying to connect to " + parentSocketAddress + " ...");
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new Context(this, parentKey);
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

    private void initConnection() throws IOException{
        Thread.ofPlatform()
                .daemon()
                .start(this::consoleRun);
        while (!Thread.interrupted()) {
//            Helpers.printKeys(selector); // for debug
//            System.out.println("Starting select");
            try {
                selector.select(this::treatKey);
                processCommand();
            } catch (UncheckedIOException tunneled) {
                throw tunneled.getCause();
            } catch (CancelledKeyException exception) {
                return ;
            }
//            System.out.println("Select finished");
        }
    }

    private void treatKey(SelectionKey key) {
//        Helpers.printSelectedKey(key); // for debug
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
                ((Context) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((Context) key.attachment()).doRead();
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "Connection closed with client due to IOException", e);
            silentlyClose(key);
        }
    }

    private void doConnect(SelectionKey key) throws IOException {
        if (!parentSocketChannel.finishConnect()) {
            return ;
        }
        var context = (Context) key.attachment();
        context.queuePacket(new ConnectPacket(address));
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
        socketKey.attach(new Context(this, socketKey));
    }

    private void silentlyClose(SelectionKey key) {
        Channel sc = (Channel) key.channel();
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
        rootTable.onNeighboursDo(src, addressContext -> addressContext.context().queuePacket(packet));
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
        } catch (IOException e) {
        }
    }

}
