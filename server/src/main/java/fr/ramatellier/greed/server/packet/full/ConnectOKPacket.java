package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ConnectOKPacket implements FullPacket, LocalPacket {
    private final IDPacket idMother;
    private final List<IDPacket> ids = new ArrayList<>();

    public ConnectOKPacket(InetSocketAddress addressMother, Set<InetSocketAddress> addressList) {
        idMother = new IDPacket(addressMother);

        for(var address: addressList) {
            ids.add(new IDPacket(address));
        }
    }

    public String getAddress() {
        return idMother.getHostname();
    }

    public int getPort() {
        return idMother.getPort();
    }

    public InetSocketAddress getMotherAddress() {
        return idMother.getSocket();
    }

    public List<InetSocketAddress> neighbours() {
        return ids.stream().map(IDPacket::getSocket).toList();
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.OK;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idMother.putInBuffer(buffer);
        buffer.putInt(ids.size());
        for(var id: ids) {
            id.putInBuffer(buffer);
        }
    }

    @Override
    public int size() {
        var res = Byte.BYTES * 2 + idMother.size() + Integer.BYTES;

        for(var id: ids) {
            res += id.size();
        }

        return res;
    }
}
