package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record LogoutRequestPacket(IDPacket id, IDPacketList daughters) implements LocalPacket {

    @Override
    public OpCodes opCode() {
        return OpCodes.LOGOUT_REQUEST;
    }

    @Override
    public void put(ByteBuffer buffer) {
        id.putInBuffer(buffer);
        daughters.putInBuffer(buffer);
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + id.size() + Integer.BYTES;

        res += daughters.size();

        return res;
    }
}
