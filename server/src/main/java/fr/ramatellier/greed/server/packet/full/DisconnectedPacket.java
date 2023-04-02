package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.reader.Reader;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class DisconnectedPacket implements BroadcastPacket, FullPacket {
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

    public IDPacket getId() {
        return id;
    }
    @Override
    public OpCodes opCode() {
        return OpCodes.DISCONNECTED;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idSrc.putInBuffer(buffer);
        id.putInBuffer(buffer);
    }


}
