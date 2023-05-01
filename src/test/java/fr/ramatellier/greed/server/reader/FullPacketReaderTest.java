package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.frame.ConnectKOPacket;
import fr.ramatellier.greed.server.packet.frame.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.component.IDComponent;
import fr.ramatellier.greed.server.packet.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.writer.FrameWriterAdapter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FullPacketReaderTest {
    private final PacketReaderAdapter readerFactory = new PacketReaderAdapter();
    @Test
    public void simpleReadPacketTest() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectOKPacket(new IDComponent((new InetSocketAddress(7777))), new IDListComponent(List.of(new IDComponent(new InetSocketAddress(7778)), new IDComponent(new InetSocketAddress(7779)))));
        var size = FrameWriterAdapter.size(okPacket);
        System.out.println("size : " + size);
        var buffer = ByteBuffer.allocate(size);
        FrameWriterAdapter.put(okPacket, buffer);
        var status = readerFactory.process(buffer, OpCodes.OK);
        assertEquals(7777, okPacket.getPort());
        assertEquals(2, okPacket.neighbours().idPacketList().size());
        assertEquals(7778, okPacket.neighbours().idPacketList().get(0).getPort());
        assertEquals(7779, okPacket.neighbours().idPacketList().get(1).getPort());
    }

    @Test
    public void simpleReadPacketTest2() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectKOPacket();
        var buffer = ByteBuffer.allocate(FrameWriterAdapter.size(okPacket));
        FrameWriterAdapter.put(okPacket, buffer);
        var status = readerFactory.process(buffer, OpCodes.KO);
        assertEquals(Reader.ProcessStatus.DONE, status);
        var packet = readerFactory.get();
        assertEquals(new ConnectKOPacket(), packet);
        assertThrows(IllegalStateException.class,() -> readerFactory.process(buffer, OpCodes.KO));
        readerFactory.reset();
        assertThrows(IllegalStateException.class, readerFactory::get);

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
