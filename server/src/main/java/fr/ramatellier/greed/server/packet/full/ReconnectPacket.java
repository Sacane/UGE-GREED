package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class ReconnectPacket implements LocalPacket {
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
    public OpCodes opCode() {
        return OpCodes.RECONNECT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        id.putInBuffer(buffer);
        buffer.putInt(ancestors.size());
        for(var ancestor: ancestors) {
            ancestor.putInBuffer(buffer);
        }
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + id.size() + Integer.BYTES;

        for(var ancestor: ancestors) {
            res += ancestor.size();
        }

        return res;
    }
}
