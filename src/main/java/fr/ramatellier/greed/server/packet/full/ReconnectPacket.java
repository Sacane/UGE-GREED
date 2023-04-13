package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;
import java.util.List;

public record ReconnectPacket(IDPacket id, List<IDPacket> ancestors) implements LocalPacket {

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
