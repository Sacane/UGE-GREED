package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class LogoutRequestPacket implements LocalPacket {
    private final IDPacket id;
    private final ArrayList<IDPacket> daughters = new ArrayList<>();

    public LogoutRequestPacket(InetSocketAddress address, List<InetSocketAddress> daughters) {
        id = new IDPacket(address);
        for(var daughter: daughters) {
            this.daughters.add(new IDPacket(daughter));
        }
    }

    public IDPacket getId() {
        return id;
    }

    public ArrayList<IDPacket> getDaughters() {
        return daughters;
    }

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
