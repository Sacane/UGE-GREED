package fr.ramatellier.greed.server.packet.sub;

import fr.ramatellier.greed.server.packet.Packet;

import java.nio.ByteBuffer;
import java.util.List;

public record IDPacketList(List<IDPacket> idPacketList) implements Packet, Part {
    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.putInt(idPacketList.size());
        for (var idPacket : idPacketList) {
            idPacket.putInBuffer(buffer);
        }
    }

    public int sizeList(){
        return idPacketList.size();
    }

    @Override
    public int size() {
        return Integer.BYTES + idPacketList.stream().mapToInt(IDPacket::size).sum();
    }
}
