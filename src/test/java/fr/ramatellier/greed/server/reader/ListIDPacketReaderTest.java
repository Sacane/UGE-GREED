package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.reader.sub.IDListReader;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListIDPacketReaderTest {

    @Test
    public void simpleReadPacketTest(){
//        var okPacket = new ConnectOKPacket(new IDPacket((new InetSocketAddress(7777))), new IDPacketList(List.of(new IDPacket(new InetSocketAddress(7778)), new IDPacket(new InetSocketAddress(7779)))));
        var idPacketList = new IDPacketList(List.of(new IDPacket(new InetSocketAddress(7778)), new IDPacket(new InetSocketAddress(7779))));
        var reader = new IDListReader();
        var buffer = ByteBuffer.allocate(idPacketList.size());
        idPacketList.putInBuffer(buffer);
        var status = reader.process(buffer);
        assertEquals(Reader.ProcessStatus.DONE, status);
        assertEquals(2, idPacketList.idPacketList().size());
        assertEquals(7778, idPacketList.idPacketList().get(0).getPort());
        assertEquals(7779, idPacketList.idPacketList().get(1).getPort());
        reader.reset();
        var buffer2 = ByteBuffer.allocate(idPacketList.size());
        idPacketList.putInBuffer(buffer2);
        var status2 = reader.process(buffer2);
        assertEquals(Reader.ProcessStatus.DONE, status2);
        assertEquals(2, idPacketList.idPacketList().size());
        assertEquals(7778, idPacketList.idPacketList().get(0).getPort());
        assertEquals(7779, idPacketList.idPacketList().get(1).getPort());
    }
}
