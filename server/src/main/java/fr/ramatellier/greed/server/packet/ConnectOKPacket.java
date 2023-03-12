package fr.ramatellier.greed.server.packet;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ConnectOKPacket implements Packet {
    private final IDPacket idMother;
    private final List<IDPacket> ids = new ArrayList<>();

    public ConnectOKPacket(InetSocketAddress addressMother, List<InetSocketAddress> addressList) {
        idMother = new IDPacket(addressMother);

        for(var address: addressList) {
            ids.add(new IDPacket(address));
        }
    }

    public String getAddress() {
        return idMother.getAddress();
    }

    public int getPort() {
        return idMother.getPort();
    }

    public InetSocketAddress getMotherAddress() {
        return idMother.getSocket();
    }

    public List<InetSocketAddress> neighbours() {
        return ids.stream().map(id -> id.getSocket()).toList();
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put((byte) 0);
        buffer.put((byte) 3);
        idMother.putInBuffer(buffer);
        buffer.putInt(ids.size());
        for(var id: ids) {
            id.putInBuffer(buffer);
        }
    }
}
