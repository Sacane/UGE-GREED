package fr.ramatellier.greed.server.context;

import fr.ramatellier.greed.server.Application;
import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.frame.Frames;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.reader.FrameReader;
import fr.ramatellier.greed.server.util.http.NonBlockingHTTPJarProvider;
import fr.ramatellier.greed.server.visitor.ReceiveFrameVisitor;
import fr.uge.ugegreed.Checker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.LongStream;

public abstract class Context {
    private static final int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    private ReceiveFrameVisitor visitor;
    protected final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    protected final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final FrameReader packetReader = new FrameReader();
    private final ArrayDeque<Frame> queue = new ArrayDeque<>();
    private boolean closed = false;
    protected final Application server;
    private final Logger logger = Logger.getLogger(Context.class.getName());

    public Context(Application server, SelectionKey key) {
        this.key = Objects.requireNonNull(key);
        this.sc = (SocketChannel) key.channel();
        this.server = server;
    }

    public void setVisitor(ReceiveFrameVisitor visitor) {
        Objects.requireNonNull(visitor);
        this.visitor = visitor;
    }

    protected void processIn() {
        for (;;) {
            var state = packetReader.process(bufferIn);
            switch (state) {
                case ERROR:
                    silentlyClose();
                case REFILL:
                    return;
                case DONE:
                    var packet = packetReader.get();
                    packetReader.reset();
                    processPacket(packet);
                    break;
            }
        }
    }

    public void handleConnection(InetSocketAddress address) {
        if(server.isRunning()) {
            logger.info("Connection accepted");
            var list = new IDListComponent(server.registeredAddresses().stream().map(IDComponent::new).toList());
            System.out.println("List of neighbors: " + list);
            var response = new ConnectOKFrame(new IDComponent(server.getAddress()),
                    list);
            queuePacket(response);
            server.addRoot(address, address, this);
            var addNodePacket = new AddNodeFrame(new IDComponent(server.getAddress()), new IDComponent(address));
            server.broadcast(addNodePacket, address);
        }
        else {
            logger.info("Connection refused");
            queuePacket(new ConnectKOFrame());
        }
    }

    private void processPacket(Frame packet) {
        switch(packet) {
            case BroadcastFrame b -> {
                b.accept(visitor);
                var oldSrc = b.src().getSocket();
                server.broadcast(b.withNewSource(new IDComponent(server.getAddress())), oldSrc);
            }
            case TransferFrame t -> {
                if(t.dst().getSocket().equals(server.getAddress())){
                    t.accept(visitor);
                } else {
                    server.transfer(t.dst().getSocket(), t);
                }
            }
            case LocalFrame l -> l.accept(visitor);
        }
    }

    public void queuePacket(Frame packet) {
        queue.offer(packet);

        processOut();
        updateInterestOps();
    }

    protected void processOut() {
        while(!queue.isEmpty()) {
            var packet = queue.peek();
            if (Frames.size(packet) <= bufferOut.remaining()) {
                queue.poll();
                Frames.put(packet, bufferOut);
            } else {
                break;
            }
        }
    }

    public void updateInterestOps() {
        var op = 0;

        if (bufferOut.position() > 0) {
            op |= SelectionKey.OP_WRITE;
        }
        if (!closed && bufferIn.hasRemaining()) {
            op |= SelectionKey.OP_READ;
        }
        if (op == 0) {
            silentlyClose();

            return;
        }

        key.interestOps(op);
    }

    private void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    public void doRead() throws IOException {
        var readValue = sc.read(bufferIn);

        if (readValue == -1) {
            closed = true;
        }

        processIn();
        updateInterestOps();
    }

    public void doWrite() throws IOException {
        bufferOut.flip();

        sc.write(bufferOut);

        bufferOut.compact();
        processOut();
        updateInterestOps();
    }


    public void processWorking(WorkRequestFrame packet) {
        if(server.isRunning()) {
            var deltaComputingPossibility = Application.MAXIMUM_COMPUTATION - server.currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server.addRoom(new ComputationEntity(new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket()),
                        new ComputeInfo(packet.checker().url(), packet.checker().className(), packet.range().start(), packet.range().end())));
                server.transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
                        packet.src(),
                        packet.dst(),
                        packet.requestId(),
                        LongComponent.of(deltaComputingPossibility)
                ));
            }
        }
        else {
            server.transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
                    packet.src(),
                    packet.dst(),
                    packet.requestId(),
                    LongComponent.of(0L)
            ));
        }
    }

    public void compute(WorkAssignmentFrame packet){
        System.out.println("Start computation...");
        var idContext = new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket());
        server.updateRoom(idContext, packet.range().start(), packet.range().end());
        var entityResponse = server.findComputationById(idContext);
        if(entityResponse.isEmpty()) {
            return ;
        }
        var entity = entityResponse.get();
        var targetRange = packet.range();

        // HTTP non-blocking
        try {
            var httpClient = NonBlockingHTTPJarProvider.fromURL(new URL(entity.info().url()));
            httpClient.onDone(body -> {
                Checker checker = Application.retrieveChecker(httpClient, entity.info().className());
                for(var i = targetRange.start(); i < targetRange.end(); i++) {
                    server.addTask(new TaskComputation(packet, checker, entity.id(), i));
                }
            });
            httpClient.launch();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            logger.severe("CANNOT GET THE CHECKER");
            LongStream.range(targetRange.start(), targetRange.end()).forEach(i -> sendBadResponse(packet, i));
        }
    }
    private void sendBadResponse(WorkAssignmentFrame origin, long index) {
        server.transfer(origin.src().getSocket(), new WorkResponseFrame(
                origin.dst(),
                origin.src(),
                origin.requestId(),
                new ResponseComponent(index, "CANNOT GET THE CHECKER", (byte) 0x03)
        ));
    }

    public void handleRequestResponse(long nbUC, LongComponent requestID, InetSocketAddress socket) {
        if(nbUC == 0){
            return;
        }
        var computeId = new ComputationIdentifier(requestID.get(), server.getAddress());
        var entity = server.retrieveWaitingComputation(computeId);
        if(entity == null){
            return;
        }
        server.incrementWaitingWorker(computeId);
        server.storeComputation(computeId, new SocketUcIdentifier(socket, nbUC));
        if(server.isRoomReady(computeId)){
            var process = new SharingProcessExecutor(
                    server.availableSocketsUc(computeId),
                    entity.info().end() - entity.info().start()
            );
            var socketRangeList = process.shareAndGet(entity.info().start());
            for(var socketRange: socketRangeList){
                var workAssignmentPacket = new WorkAssignmentFrame(
                        new IDComponent(server.getAddress()),
                        new IDComponent(socketRange.socketAddress()),
                        requestID,
                        new RangeComponent(socketRange.range().start(), socketRange.range().end())
                );
                server.transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }

    public void handleResponse(ResponseComponent responseComponent, Long requestID, String result) {
        switch(responseComponent.getResponseCode()) {
            case 0x00 -> System.out.println(responseComponent.getResponse().value());
            case 0x01 -> System.out.println("An exception has occur while computing the value : " + responseComponent.getValue());
            case 0x02 -> System.out.println("Time out while computing the value : " + responseComponent.getValue());
            case 0x03 -> System.out.println("Cannot get the checker");
            default -> logger.severe("UNKNOWN RESPONSE CODE");
        }

        var id = new ComputationIdentifier(requestID, server.getAddress());
        try {
            server.treatComputationResult(id, result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "The file cannot be written : ", e);
        }
    }

    public void updateRoot(InetSocketAddress src, InetSocketAddress dst, Context context) {
        server.addRoot(src, dst, context);
    }
}
