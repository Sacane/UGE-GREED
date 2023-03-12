package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.ConnectKOPacket;

import java.nio.ByteBuffer;

public class ConnectKOPacketReader implements Reader<ConnectKOPacket>{
    @Override
    public ProcessStatus process(ByteBuffer bb) {
        return null;
    }

    @Override
    public ConnectKOPacket get() {
        return null;
    }

    @Override
    public void reset() {

    }
}
