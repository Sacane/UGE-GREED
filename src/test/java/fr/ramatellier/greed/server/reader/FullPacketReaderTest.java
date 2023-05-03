package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.model.ConnectKOFrame;
import fr.ramatellier.greed.server.frame.model.ConnectOKFrame;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.util.OpCode;
import fr.ramatellier.greed.server.frame.Frames;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FullPacketReaderTest {
    private final FrameReaderDecoder readerFactory = new FrameReaderDecoder();
    @Test
    public void simpleReadPacketTest() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectOKFrame(new IDComponent((new InetSocketAddress(7777))), new IDListComponent(List.of(new IDComponent(new InetSocketAddress(7778)), new IDComponent(new InetSocketAddress(7779)))));
        var size = Frames.size(okPacket);
        System.out.println("size : " + size);
        var buffer = ByteBuffer.allocate(size);
        Frames.put(okPacket, buffer);
        readerFactory.process(buffer, OpCode.OK);
        assertEquals(7777, okPacket.getPort());
        assertEquals(2, okPacket.neighbours().idPacketList().size());
        assertEquals(7778, okPacket.neighbours().idPacketList().get(0).getPort());
        assertEquals(7779, okPacket.neighbours().idPacketList().get(1).getPort());
    }

    @Test
    public void simpleReadPacketTest2() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        var okPacket = new ConnectKOFrame();
        var buffer = ByteBuffer.allocate(Frames.size(okPacket));
        Frames.put(okPacket, buffer);
        var status = readerFactory.process(buffer, OpCode.KO);
        assertEquals(Reader.ProcessStatus.DONE, status);
        var packet = readerFactory.get();
        assertEquals(new ConnectKOFrame(), packet);
        assertThrows(IllegalStateException.class,() -> readerFactory.process(buffer, OpCode.KO));
        readerFactory.reset();
        assertThrows(IllegalStateException.class, readerFactory::get);

    }
}
