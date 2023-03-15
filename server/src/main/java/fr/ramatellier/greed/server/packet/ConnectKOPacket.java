package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ConnectKOPacket implements FullPacket{

    private final String localAddress;
    public ConnectKOPacket(String address) {
        this.localAddress = Objects.requireNonNull(address);
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
        buffer.put(OpCodes.KO);
        var ipPacket = new IPPacket(localAddress);
        ipPacket.putInBuffer(buffer);
    }

    @Override
    public void accept(PacketVisitor visitor) {
        visitor.visit(this);
    }
}
