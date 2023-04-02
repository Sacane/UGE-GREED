package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public final class ConnectKOPacket implements FullPacket, LocalPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.KO;
    }
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
    }
}
