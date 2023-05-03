package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.frame.model.BroadcastFrame;
import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.frame.model.LocalFrame;
import fr.ramatellier.greed.server.frame.model.TransferFrame;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.reader.FrameReader;
import fr.ramatellier.greed.server.visitor.ReceiveFrameVisitor;
import fr.ramatellier.greed.server.frame.Frames;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;

public abstract class Context {
    private static final int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    private final ReceiveFrameVisitor visitor;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final FrameReader packetReader = new FrameReader();
    private final ArrayDeque<Frame> queue = new ArrayDeque<>();
    private boolean closed = false;
    private final Server server;

    public Context(Server server, SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.visitor = new ReceiveFrameVisitor(server, this);
        this.server = server;
    }

    private void processIn() {
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

    private void processOut() {
        while(!queue.isEmpty()) {
            var packet = queue.peek();
            try {
                if (Frames.size(packet) <= bufferOut.remaining()) {
                    queue.poll();
                    Frames.put(packet, bufferOut);
                } else {
                    break;
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                System.out.println("Error while writing packet");
                silentlyClose();
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
}
