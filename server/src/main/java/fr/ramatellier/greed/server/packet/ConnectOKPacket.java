package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ConnectOKPacket implements FullPacket {
    private final IDPacket idMother;
    private final List<IDPacket> ids = new ArrayList<>();

    public ConnectOKPacket(InetSocketAddress addressMother, Set<InetSocketAddress> addressList) {
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
        return ids.stream().map(IDPacket::getSocket).toList();
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.OK.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(kind().BYTES);
        buffer.put(opCode());
        idMother.putInBuffer(buffer);
        buffer.putInt(ids.size());
        for(var id: ids) {
            id.putInBuffer(buffer);
        }
    }
}
