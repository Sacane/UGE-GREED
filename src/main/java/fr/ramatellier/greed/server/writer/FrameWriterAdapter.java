package fr.ramatellier.greed.server.writer;

import fr.ramatellier.greed.server.packet.component.GreedComponent;
import fr.ramatellier.greed.server.packet.frame.Frame;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class FrameWriterAdapter {

    public static <T extends Frame> int size(T recordPacket) throws InvocationTargetException, IllegalAccessException {
        int size = Byte.BYTES * 2;
        var recordClass = recordPacket.getClass();
        var components = recordClass.getRecordComponents();
        for(var component: components){
            var type = component.getType();
            if(type == byte.class || type == Byte.class) {
                size += Byte.BYTES;
            }else if(type == int.class || type == Integer.class){
                size += Integer.BYTES;
            } else if(type == long.class || type == Long.class){
                size += Long.BYTES;
            } else if(GreedComponent.class.isAssignableFrom(type)){
                GreedComponent packet = (GreedComponent) component.getAccessor().invoke(recordPacket);
                size += packet.size();
            }
        }
        return size;
    }

    public static <T extends Frame> void put(T frame, ByteBuffer buffer) throws InvocationTargetException, IllegalAccessException {
        var recordClass = frame.getClass();
        var components = recordClass.getRecordComponents();
        buffer.put(frame.kind().BYTES).put(frame.opCode().BYTES);
        for(var component: components){
            var type = component.getType();
            if(type == byte.class || type == Byte.class) {
                byte b = (byte) component.getAccessor().invoke(frame);
                buffer.put(b);
            }else if(type == int.class || type == Integer.class){
                int i = (int) component.getAccessor().invoke(frame);
                buffer.putInt(i);
            } else if(type == long.class || type == Long.class){
                long l = (long) component.getAccessor().invoke(frame);
                buffer.putLong(l);
            } else if(GreedComponent.class.isAssignableFrom(type)) {
                GreedComponent packet = (GreedComponent) component.getAccessor().invoke(frame);
                packet.putInBuffer(buffer);
            }
        }
    }
}
