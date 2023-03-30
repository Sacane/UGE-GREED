package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class DisconnectedPacket implements FullPacket {
    private final IDPacket idSrc;
    private final IDPacket id;

    public DisconnectedPacket(InetSocketAddress idSrc, InetSocketAddress id) {
        this.idSrc = new IDPacket(idSrc);
        this.id = new IDPacket(id);
    }

    public IDPacket getIdSrc() {
        return idSrc;
    }

    public IDPacket getId() {
        return id;
    }

    @Override
    public TramKind kind() {
        return TramKind.BROADCAST;
    }

    @Override
    public byte opCode() {
        return OpCodes.DISCONNECTED.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idSrc.putInBuffer(buffer);
        id.putInBuffer(buffer);
    }
}
