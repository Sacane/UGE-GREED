package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.ConnectKOPacket;
import fr.ramatellier.greed.server.packet.full.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.util.OpCodes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FullPacketReaderTest {
    private final MultiReader reader = new MultiReader();
    @Test
    public void simpleReadPacketTest() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectOKPacket(new IDPacket((new InetSocketAddress(7777))), new IDPacketList(List.of(new IDPacket(new InetSocketAddress(7778)), new IDPacket(new InetSocketAddress(7779)))));
        var buffer = ByteBuffer.allocate(okPacket.size());
        okPacket.put(buffer);
        var status = reader.process(buffer, OpCodes.OK);
        assertEquals(Reader.ProcessStatus.DONE, status);
        assertEquals(7777, okPacket.getPort());
        assertEquals(2, okPacket.neighbours().idPacketList().size());
        assertEquals(7778, okPacket.neighbours().idPacketList().get(0).getPort());
        assertEquals(7779, okPacket.neighbours().idPacketList().get(1).getPort());
    }

    @Test
    public void simpleReadPacketTest2() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectKOPacket();
        var buffer = ByteBuffer.allocate(okPacket.size());
        okPacket.put(buffer);
        var status = reader.process(buffer, OpCodes.KO);
        assertEquals(Reader.ProcessStatus.DONE, status);
        var packet = reader.get();
        assertEquals(new ConnectKOPacket(), packet);
        assertThrows(IllegalStateException.class,() -> reader.process(buffer, OpCodes.KO));
        reader.reset();
        assertThrows(IllegalStateException.class, reader::get);

    }

//    public void simpleReadPacketTest3() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//        var buffer = ByteBuffer.allocate(okPacket.size());
//        okPacket.put(buffer);
//        var status = reader.process(buffer, OpCodes.KO);
//        assertEquals(Reader.ProcessStatus.DONE, status);
//        var packet = reader.get();
//        assertEquals(new ConnectKOPacket(), packet);
//        assertThrows(IllegalStateException.class,() -> reader.process(buffer, OpCodes.KO));
//        reader.reset();
//        assertThrows(IllegalStateException.class, reader::get);
//
//    }
}
