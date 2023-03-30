package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class ReconnectPacket implements FullPacket {
    private final IDPacket id;
    private final List<IDPacket> ancesters = new ArrayList<>();

    public ReconnectPacket(InetSocketAddress idAddress, List<InetSocketAddress> addresses) {
        id = new IDPacket(idAddress);

        for(var address: addresses) {
            ancesters.add(new IDPacket(address));
        }
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.RECONNECT.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        id.putInBuffer(buffer);
        buffer.putInt(ancesters.size());
        for(var ancestor: ancesters) {
            ancestor.putInBuffer(buffer);
        }
    }
}
