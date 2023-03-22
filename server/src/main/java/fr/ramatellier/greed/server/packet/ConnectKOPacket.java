package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

public class ConnectKOPacket implements FullPacket {
    public ConnectKOPacket() {
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.KO;
    }
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode());
    }

    @Override
    public void accept(PacketVisitor visitor) {
        visitor.visit(this);
    }
}
