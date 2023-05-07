package fr.ramatellier.greed.server.frame;

import fr.ramatellier.greed.server.frame.component.GreedComponent;
import fr.ramatellier.greed.server.frame.model.Frame;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * This class is a utility class for {@link Frame} and {@link GreedComponent}.
 * It provides encoding and size methods for a frame.
 */
public final class Frames {

    @FunctionalInterface
    private interface GreedComponentConsumer {
        void accept(Frame frame, Consumer<? super GreedComponent> greedConsumer);
    }
    private Frames(){}
    private final static ClassValue<GreedComponentConsumer> CACHE = new ClassValue<>() {
        @Override
        protected GreedComponentConsumer computeValue(Class<?> type) {
            return (frame, greedConsumer) -> {
                for(var component: type.getRecordComponents()){
                    GreedComponent packet = (GreedComponent) invoke(component.getAccessor(), frame);
                    greedConsumer.accept(packet);
                }
            };
        }
    };

    public static <T extends Frame> int size(T frame) {
        var sizes = new int[2];
        sizes[0] = Byte.BYTES * 2;
        CACHE.get(frame.getClass())
                .accept(frame, packet -> sizes[1] += packet.size());
        return sizes[0] + sizes[1];
    }

    public static <T extends Frame> void encode(T frame, ByteBuffer buffer) {
        var opCode = OpCode.of(frame);
        buffer.put(frame.kind().BYTES).put(opCode.BYTES);
        CACHE.get(frame.getClass()).accept(frame, packet -> packet.putInBuffer(buffer));
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
