package fr.ramatellier.greed.server.frame;

import fr.ramatellier.greed.server.frame.component.GreedComponent;
import fr.ramatellier.greed.server.frame.model.Frame;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class Frames {

    private Frames(){}

    public static <T extends Frame> int size(T recordPacket) throws InvocationTargetException, IllegalAccessException {
        var sizes = new int[5];
        sizes[0] += Byte.BYTES * 2;
        adapt(recordPacket,
                __ -> sizes[1] += Byte.BYTES,
                __ -> sizes[2] += Integer.BYTES,
                __ -> sizes[3] += Long.BYTES,
                packet -> sizes[4] += packet.size());
        return Arrays.stream(sizes).sum();
    }

    public static <T extends Frame> void put(T frame, ByteBuffer buffer) throws InvocationTargetException, IllegalAccessException {
        buffer.put(frame.kind().BYTES).put(frame.opCode().BYTES);
        adapt(frame, buffer::put, buffer::putInt, buffer::putLong, packet -> packet.putInBuffer(buffer));
    }

    private static <T extends Frame> void adapt(T frame, Consumer<Byte> byteConsumer, IntConsumer intConsumer, LongConsumer longConsumer, Consumer<GreedComponent> greedConsumer) throws InvocationTargetException, IllegalAccessException {
        var components = frame.getClass().getRecordComponents();
        for(var component: components){
            var type = component.getType();
            if(type == byte.class || type == Byte.class) {
                byteConsumer.accept((byte) component.getAccessor().invoke(frame));
            }else if(type == int.class || type == Integer.class){
                int i = (int) component.getAccessor().invoke(frame);
                intConsumer.accept(i);
            } else if(type == long.class || type == Long.class){
                long l = (long) component.getAccessor().invoke(frame);
                longConsumer.accept(l);
            } else if(GreedComponent.class.isAssignableFrom(type)) {
                GreedComponent packet = (GreedComponent) component.getAccessor().invoke(frame);
                greedConsumer.accept(packet);
            }
        }
    }
}
