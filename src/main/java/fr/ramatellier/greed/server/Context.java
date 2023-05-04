package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.frame.Frames;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.model.BroadcastFrame;
import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.frame.model.LocalFrame;
import fr.ramatellier.greed.server.frame.model.TransferFrame;
import fr.ramatellier.greed.server.reader.FrameReader;
import fr.ramatellier.greed.server.visitor.FrameVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Objects;

public abstract class Context {
    private static final int BUFFER_SIZE = 32_768;
    protected final SelectionKey key;
    protected final SocketChannel sc;
    private FrameVisitor visitor;
    protected final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    protected final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final FrameReader packetReader = new FrameReader();
    private final ArrayDeque<Frame> queue = new ArrayDeque<>();
    private boolean closed = false;
    protected final Server server;

    public Context(Server server, SelectionKey key) {
        this.key = Objects.requireNonNull(key);
        this.sc = (SocketChannel) key.channel();
        this.server = server;
    }

    public void setVisitor(FrameVisitor visitor) {
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


}
