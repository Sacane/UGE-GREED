package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.Packet;
import fr.ramatellier.greed.server.reader.PacketReader;
import fr.ramatellier.greed.server.reader.Reader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;

public class Context {
    private static final int BUFFER_SIZE = 1_024;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ServerVisitor visitor;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final PacketReader packetReader = new PacketReader();
    private final ArrayDeque<Packet> queue = new ArrayDeque<>();
    private boolean closed = false;

    public Context(Server server, SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.visitor = new ServerVisitor(server, this);
    }
    public String src(){
        var address = (InetSocketAddress) sc.socket().getRemoteSocketAddress();
        return address.getHostName();
    }

    private void processIn() {
        for (;;) {
            Reader.ProcessStatus status = packetReader.process(bufferIn);
            switch (status) {
                case DONE:
                    var packet = packetReader.get();
                    packetReader.reset();
                    packet.accept(visitor);
                    break;
                case REFILL:
                    return;
                case ERROR:
                    silentlyClose();
                    return;
            }
        }
    }


    public void queuePacket(Packet packet) {
        queue.add(packet);

        processOut();
        updateInterestOps();
    }

    private void processOut() {
        while(!queue.isEmpty()) {
            var packet = queue.poll();

            packet.putInBuffer(bufferOut);
        }
    }

    private void updateInterestOps() {
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
