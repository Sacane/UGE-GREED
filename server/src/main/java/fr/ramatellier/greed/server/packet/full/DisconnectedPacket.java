package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class DisconnectedPacket implements BroadcastPacket {
    private final IDPacket idSrc;
    private final IDPacket id;

    public DisconnectedPacket(InetSocketAddress idSrc, InetSocketAddress id) {
        this.idSrc = new IDPacket(idSrc);
        this.id = new IDPacket(id);
    }

    @Override
    public IDPacket src() {
        return idSrc;
    }

    @Override
    public BroadcastPacket withNewSource(IDPacket newSrc) {
        return new DisconnectedPacket(newSrc.getSocket(), id.getSocket());
    }

    public IDPacket id() {
        return id;
    }
    @Override
    public OpCodes opCode() {
        return OpCodes.DISCONNECTED;
    }

    @Override
    public void put(ByteBuffer buffer) {
        idSrc.putInBuffer(buffer);
        id.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + idSrc.size() + id.size();
    }
}
