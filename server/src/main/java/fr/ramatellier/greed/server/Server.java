package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.ConnectPacket;
import fr.ramatellier.greed.server.packet.FullPacket;
import fr.ramatellier.greed.server.packet.WorkRequestPacket;
import fr.ramatellier.greed.server.util.RootTable;

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

import static java.lang.Long.parseLong;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    // Self server field
    private final ServerSocketChannel serverSocketChannel;
    private SelectionKey serverKey;
    private final Selector selector;
    private boolean isRunning = true;
    private final boolean isRoot;
    private final InetSocketAddress address;
    private final RootTable rootTable = new RootTable();
    private ServerState state = ServerState.STOPPED;
    private final ArrayBlockingQueue<CommandArgs> commandQueue = new ArrayBlockingQueue<>(10);

    // Parent information
    private final SocketChannel parentSocketChannel;
    private final InetSocketAddress parentSocketAddress;
    private SelectionKey parentKey;

    // Others

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    enum Command{
        INFO, STOP, SHUTDOWN, COMPUTE
    }
    public record CommandArgs(Command command, String[] args) {
    }

    enum ServerState{
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


    private void sendCommand(CommandArgs command) throws InterruptedException{
        synchronized (commandQueue){
            commandQueue.put(command);
            selector.wakeup();
        }
    }

    private void consoleRun(){
        try{
            try(var scan = new Scanner(System.in)){
                while(scan.hasNextLine()){
                    var line = scan.nextLine().split(" ");
                    switch(line[0]){
                        case "INFO" -> sendCommand(new CommandArgs(Command.INFO, null));
                        case "STOP" -> sendCommand(new CommandArgs(Command.STOP, null));
                        case "SHUTDOWN" -> sendCommand(new CommandArgs(Command.SHUTDOWN, null));
                        case "COMPUTE" -> sendCommand(new CommandArgs(Command.COMPUTE, new String[]{line[1], line[2], line[3], line[4]}));
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
        if(dst.equals(address)){
            return;
        }
        rootTable.sendTo(dst, packet);
    }
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

    public Set<InetSocketAddress> registeredAddresses() {
        return rootTable.registeredAddresses();
    }

    public static void launchRoot(int port) throws IOException {
        new Server(port).launch();
    }

    public static void launchConnected(int hostPort, String IP, int connectPort) throws IOException {
        Objects.requireNonNull(IP, "IP can't be null");
        new Server(hostPort, IP, connectPort).connect();
    }
    private void printInfo(){
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
        processComputeCommand(new ComputeInfo(args[0], args[1], parseLong(args[2]), parseLong(args[3])));
        /*System.out.println("Please type the following information in the next line to start a computing service :");
        System.out.println("[URL] [CLASS-NAME] [START as LONG] [END as LONG]");
        var scanner = new Scanner(System.in);
        var line = scanner.nextLine();
        System.out.println("LINE --> " + line);
        var commandParser = new ComputeCommandParser(line);
        if(!commandParser.check()){
            System.out.println("The computation command is not valid");
            return;
        }
        processComputeCommand(commandParser.get());*/
    }

    private void processComputeCommand(ComputeInfo info) {
        var workers = rootTable.allAddress();

        for(var worker: workers) {
            var packet = new WorkRequestPacket(address, worker.address(), 0, info.url(), info.className(), info.start(), info.end(), 0);

            worker.context().queuePacket(packet);
        }
    }

    public void connect() throws IOException {
        if(isRoot || parentSocketAddress == null) {
            throw new IllegalStateException("This server is a root server");
        }
        logger.info("Trying to connect to " + parentSocketAddress + " ...");
        parentSocketChannel.configureBlocking(false);
        parentSocketChannel.connect(parentSocketAddress);
        parentKey = parentSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        var context = new Context(this, parentKey);
        parentKey.attach(context);
        state = ServerState.ON_GOING;
        initConnection();
    }

    public void launch() throws IOException {
        if(!isRoot) {
            throw new IllegalStateException("This server is not a root server");
        }
        state = ServerState.ON_GOING;
        serverSocketChannel.configureBlocking(false);
        serverKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("Server started on " + address);
        state = ServerState.ON_GOING;
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
        if (!parentSocketChannel.finishConnect()){
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
}
