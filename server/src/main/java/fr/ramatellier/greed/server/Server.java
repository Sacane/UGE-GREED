package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.ConnectPacket;
import fr.ramatellier.greed.server.packet.FullPacket;
import fr.ramatellier.greed.server.util.RootTable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocketChannel serverSocketChannel;
    private final SocketChannel parentSocketChannel;
    private final InetSocketAddress parentSocketAddress;
    private SelectionKey serverKey;
    private SelectionKey parentKey;
    private final Selector selector;
    private final InetSocketAddress address;
    private boolean isRunning = true;
    private final boolean isRoot;
    private final RootTable rootTable = new RootTable();
    private ServerState state = ServerState.STOPPED;
    private final ArrayBlockingQueue<Command> commandQueue = new ArrayBlockingQueue<>(10);

    enum Command{
        INFO, STOP, SHUTDOWN
    }

    public void transfer(InetSocketAddress dst, FullPacket packet) {
        if(dst.equals(address)){
            return;
        }
        var addressContext = rootTable.closestNeighbourOf(dst);
        addressContext.context().queuePacket(packet);
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

    private void sendCommand(Command command) throws InterruptedException{
        synchronized (commandQueue){
            commandQueue.put(command);
            selector.wakeup();
        }
    }

    private void consoleRun(){
        try{
            var scan = new Scanner(System.in);
            while(scan.hasNextLine()){
                var line = scan.nextLine();
                switch(line){
                    case "INFO" -> sendCommand(Command.INFO);
                    case "STOP" -> sendCommand(Command.STOP);
                    case "SHUTDOWN" -> sendCommand(Command.SHUTDOWN);
                    default -> System.out.println("Unknown command");
                }
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        } finally {
            logger.info("Console Thread has been stopped");
        }
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
            System.out.println(rootTable);
        }
    }

    public Set<InetSocketAddress> neighbours() {
        return rootTable.neighbours();
    }

    private static Server createROOT(int port) throws IOException {
        return new Server(port);
    }

    private static Server createCONNECTED(int hostPort, String IP, int connectPort) throws IOException {
        Objects.requireNonNull(IP, "IP can't be null");
        return new Server(hostPort, IP, connectPort);
    }

    void processCommand(){
        for(;;){
            var command = commandQueue.poll();
            if(command == null){
                return;
            }
            switch(command){
                case INFO -> {
                    logger.info("Command INFO received");
                    rootTable.onNeighbours(address, info -> System.out.println(info.address()));
                }
                case STOP -> {
                    logger.info("Command STOP received");
                }
                case SHUTDOWN -> {
                    logger.info("Command SHUTDOWN received");
                }
            }
        }
    }

    private void connect() throws IOException {
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

    public void broadcast(FullPacket packet, InetSocketAddress src) {
        rootTable.onNeighbours(src, addressContext -> addressContext.context().queuePacket(packet));
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1 && args.length != 3) {
            usage();
            return;
        }

        if(args.length == 1) {
            createROOT(Integer.parseInt(args[0])).launch();
        }
        else {
            createCONNECTED(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2])).connect();
        }
    }

    private static void usage() {
        System.out.println("Usage (ROOT MODE) : Server port");
        System.out.println("Usage (CONNECTED MODE) : Server port IP port");
    }
}
