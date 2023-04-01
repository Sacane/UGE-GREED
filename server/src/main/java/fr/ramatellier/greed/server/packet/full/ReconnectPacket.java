package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class ReconnectPacket implements FullPacket {
    private final IDPacket id;
    private final List<IDPacket> ancestors = new ArrayList<>();

    public ReconnectPacket(InetSocketAddress idAddress, List<InetSocketAddress> addresses) {
        id = new IDPacket(idAddress);

        for(var address: addresses) {
            ancestors.add(new IDPacket(address));
        }
    }

    public IDPacket getId() {
        return id;
    }

    public List<IDPacket> getAncestors() {
        return ancestors;
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.RECONNECT.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        id.putInBuffer(buffer);
        buffer.putInt(ancestors.size());
        for(var ancestor: ancestors) {
            ancestor.putInBuffer(buffer);
        }
    }
}
