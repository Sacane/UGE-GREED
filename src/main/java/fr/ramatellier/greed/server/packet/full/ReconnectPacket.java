package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record ReconnectPacket(IDPacket id, IDPacketList ancestors) implements LocalPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.RECONNECT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        id.putInBuffer(buffer);
        ancestors.putInBuffer(buffer);
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + id.size() + Integer.BYTES;
        res += ancestors.size();
        return res;
    }
}
