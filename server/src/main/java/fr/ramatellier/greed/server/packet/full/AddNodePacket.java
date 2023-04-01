package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;
import java.util.Objects;

public record AddNodePacket(IDPacket src, IDPacket daughter) implements FullPacket {
    public AddNodePacket(IDPacket src, IDPacket daughter) {
        this.src = Objects.requireNonNull(src);
        this.daughter = Objects.requireNonNull(daughter);
    }

    @Override
    public TramKind kind() {
        return TramKind.BROADCAST;
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        src.putInBuffer(buffer);
        daughter.putInBuffer(buffer);
    }
}
