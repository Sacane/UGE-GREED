package fr.ramatellier.greed.server.packet;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;

public class CheckerPacket implements Packet {
    private final URL url;
    private final String className;

    public CheckerPacket(URL url, String className){
        this.url = Objects.requireNonNull(url);
        this.className = Objects.requireNonNull(className);
    }



    @Override
    public void putInBuffer(ByteBuffer buffer) {
    }
}
