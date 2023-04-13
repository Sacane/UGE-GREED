package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;
import java.util.List;

public record LogoutRequestPacket(IDPacket id, List<IDPacket> daughters) implements LocalPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_REQUEST;
    }

    @Override
    public void put(ByteBuffer buffer) {
        id.putInBuffer(buffer);
        buffer.putInt(daughters.size());
        for(var daughter: daughters) {
            daughter.putInBuffer(buffer);
        }
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + id.size() + Integer.BYTES;

        for(var daughter: daughters) {
            res += daughter.size();
        }

        return res;
    }
}
