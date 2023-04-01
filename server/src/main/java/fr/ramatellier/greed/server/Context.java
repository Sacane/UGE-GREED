package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.reader.PacketReader;
import fr.ramatellier.greed.server.visitor.ReceivePacketVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;

public class Context {
    private static final int BUFFER_SIZE = 32_560;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ReceivePacketVisitor visitor;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final PacketReader packetReader = new PacketReader();
    private final ArrayDeque<FullPacket> queue = new ArrayDeque<>();
    private boolean closed = false;

    public Context(Server server, SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.visitor = new ReceivePacketVisitor(server, this);
    }

    private void processIn() {
        for (;;) {
            var status = packetReader.process(bufferIn);
            switch (status) {
                case DONE -> {
                    var packet = packetReader.get();
                    packetReader.reset();
                    packet.accept(visitor);
                }
                case REFILL -> {
                    return;
                }
                case ERROR -> {
                    System.out.println("ERROR IN CONTEXT");
                    silentlyClose();
                }
            }
        }
    }

    public void queuePacket(FullPacket packet) {
        System.out.println("QUEUE PACKET");
        queue.offer(packet);
        processOut();
        updateInterestOps();
    }

    private void processOut() {
        while(!queue.isEmpty()) {
            var packet = queue.peek();
            if(packet == null){
                return;
            }
            var buffer = ByteBuffer.allocate(BUFFER_SIZE);
            packet.putInBuffer(buffer);
            buffer.flip();
            System.out.println(packet);
            if(buffer.remaining() <= bufferOut.remaining()) {
                bufferOut.put(buffer);
                queue.poll();
            } else {
                return;
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
        System.out.println("DO READ"); // Ne s'applique qu'une fois par serveur lors d'une réponse à un calcul...
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
