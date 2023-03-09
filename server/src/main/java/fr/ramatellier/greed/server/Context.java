package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.reader.StringReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

public class Context {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final int BUFFER_SIZE = 1_024;
    private final SelectionKey key;
    private final SocketChannel sc;
    private final ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE);
    private final ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE);
    private final StringReader stringReader = new StringReader();
    private final ArrayDeque<String> queue = new ArrayDeque<>();
    private final Server server; // we could also have Context as an instance class, which would naturally
    // give access to ServerChatInt.this
    private boolean closed = false;

    public Context(Server server, SelectionKey key) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.server = server;
    }

    private void processIn() {
        for (;;) {
            Reader.ProcessStatus status = stringReader.process(bufferIn);
            switch (status) {
                case DONE:
                    var msg = stringReader.get();
                    System.out.println(msg);
                    stringReader.reset();
                    break;
                case REFILL:
                    return;
                case ERROR:
                    silentlyClose();
                    return;
            }
        }
    }

    public void queuePacket(String packet) {
        queue.add(packet);

        processOut();
        updateInterestOps();
    }

    private void processOut() {
        while(bufferOut.remaining() >= 1024 && !queue.isEmpty()) {
            var msg = queue.poll();
            var send = UTF8.encode(msg);

            bufferOut.putInt(send.remaining());
            bufferOut.put(send);
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
