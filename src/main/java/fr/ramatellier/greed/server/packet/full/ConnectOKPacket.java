package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

public record ConnectOKPacket(IDPacket idMother, List<IDPacket> neighbours) implements LocalPacket {


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
        buffer.putInt(neighbours.size());
        for(var id: neighbours) {
            id.putInBuffer(buffer);
        }
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + idMother.size() + Integer.BYTES;

        for(var id: neighbours) {
            res += id.size();
        }

        return res;
    }
}
