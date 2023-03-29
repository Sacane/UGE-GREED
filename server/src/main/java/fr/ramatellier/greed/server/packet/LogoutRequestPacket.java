package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class LogoutRequestPacket implements FullPacket {
    private final ArrayList<IDPacket> daughters = new ArrayList<>();

    public LogoutRequestPacket(List<InetSocketAddress> daughters) {
        for(var daughter: daughters) {
            this.daughters.add(new IDPacket(daughter));
        }
    }

    public ArrayList<IDPacket> getDaughters() {
        return daughters;
    }

    @Override
    public TramKind kind() {
        return TramKind.LOCAL;
    }

    @Override
    public byte opCode() {
        return OpCodes.LOGOUT_REQUEST.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        buffer.putInt(daughters.size());
        for(var daughter: daughters) {
            daughter.putInBuffer(buffer);
        }
    }
}
