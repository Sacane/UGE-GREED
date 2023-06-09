package fr.ramatellier.greed.server.frame.component;

import java.nio.ByteBuffer;
import java.util.List;

public record IDListComponent(List<IDComponent> idPacketList) implements GreedComponent {
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
        return Integer.BYTES + idPacketList.stream().mapToInt(IDComponent::size).sum();
    }

}
