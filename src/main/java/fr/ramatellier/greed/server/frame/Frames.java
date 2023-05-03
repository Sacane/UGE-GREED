package fr.ramatellier.greed.server.frame;

import fr.ramatellier.greed.server.frame.component.GreedComponent;
import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.util.OpCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.*;

public final class Frames {

    @FunctionalInterface
    private interface GreedComponentConsumer {
        void accept(Frame frame, Consumer<Byte> byteConsumer, IntConsumer intConsumer, LongConsumer longConsumer, Consumer<GreedComponent> greedConsumer);
    }
    private Frames(){}
    private final static ClassValue<GreedComponentConsumer> CACHE = new ClassValue<>() {
        @Override
        protected GreedComponentConsumer computeValue(Class<?> type) {
            return (frame, byteConsumer, intConsumer, longConsumer, greedConsumer) -> {
                for(var component: type.getRecordComponents()){
                    var accessor = component.getAccessor();
                    var opCode = component.getType();
                    if(opCode == byte.class || opCode == Byte.class) {
                        byteConsumer.accept((byte) invoke(accessor, frame));
                    }else if(opCode == int.class || opCode == Integer.class){
                        int i = (int) invoke(accessor, frame);
                        intConsumer.accept(i);
                    } else if(opCode == long.class || opCode == Long.class){
                        long l = (long) invoke(accessor, frame);
                        longConsumer.accept(l);
                    } else if(GreedComponent.class.isAssignableFrom(opCode)) {
                        GreedComponent packet = (GreedComponent) invoke(accessor, frame);
                        greedConsumer.accept(packet);
                    }
                }
            };
        }
    };

    public static <T extends Frame> int size(T frame) {
        var sizes = new int[5];
        sizes[0] = Byte.BYTES * 2;
        CACHE.get(frame.getClass())
                .accept(frame,
                        __ -> sizes[1] += Byte.BYTES,
                        __ -> sizes[2] += Integer.BYTES,
                        __ -> sizes[3] += Long.BYTES,
                        packet -> sizes[4] += packet.size());
        return Arrays.stream(sizes).sum();
    }

    public static <T extends Frame> void put(T frame, ByteBuffer buffer) {
        var opCode = OpCode.fromFrame(frame);
        buffer.put(frame.kind().BYTES).put(opCode.BYTES);
        CACHE.get(frame.getClass())
                .accept(frame,
                        buffer::put,
                        buffer::putInt,
                        buffer::putLong,
                        packet -> packet.putInBuffer(buffer));
    }

    private static Object invoke(Method accessor, Object object) {
        try {
            return accessor.invoke(object);
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new UndeclaredThrowableException(e);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
