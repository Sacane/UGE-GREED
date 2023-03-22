package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class AddNodePacket implements FullPacket {
    private final IDPacket src;
    private final IDPacket daughter;

    public AddNodePacket(IDPacket src, IDPacket daughter) {
        this.src = Objects.requireNonNull(src);
        this.daughter = Objects.requireNonNull(daughter);
    }

    public IDPacket getSrc() {
        return src;
    }

    public IDPacket getDaughter() {
        return daughter;
    }

    @Override
    public void accept(PacketVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TramKind kind() {
        return TramKind.BROADCAST;
    }

    @Override
    public byte opCode() {
        return OpCodes.ADD_NODE;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode());
        src.putInBuffer(buffer);
        daughter.putInBuffer(buffer);
    }
}
