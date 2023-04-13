package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.util.OpCodes;

import java.nio.ByteBuffer;

public record ConnectOKPacket(IDPacket idMother, IDPacketList neighbours) implements LocalPacket {


    public int getPort() {
        return idMother.getPort();
    }


    @Override
    public OpCodes opCode() {
        return OpCodes.OK;
    }

    @Override
    public void put(ByteBuffer buffer) {
        idMother.putInBuffer(buffer);
        neighbours.putInBuffer(buffer);
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + idMother.size() + Integer.BYTES;

        res += neighbours.size();

        return res;
    }
}
