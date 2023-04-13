package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.sub.*;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.reader.primitive.LongReader;
import fr.ramatellier.greed.server.reader.sub.*;
import fr.ramatellier.greed.server.util.OpCodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ReaderFactory {

    private final static ClassValue<List<Function<Record, Reader<?>>>> CACHE = new ClassValue<>() {
        @Override
        protected List<Function<Record, Reader<?>>> computeValue(Class<?> type) {
            return Arrays.stream(type.getRecordComponents()).map(ReaderFactory::build).toList();
        }
    };

    private static Function<Record, Reader<?>> build(RecordComponent component){
        var accessor = component.getAccessor();
        var proxy = proxy(invoke(accessor, component));
        if(proxy == null){
            throw new AssertionError();
        }
        return record -> toReader(proxy);
    }

    private static Part proxy(Object objet){
        if(objet instanceof Part part){
            return part;
        }
        return null;
    }

    private static Reader<?> toReader(Part record){
        return switch(record){
            case BytePacketPart ignored -> new ByteReader();
            case LongPacketPart ignored -> new LongReader();
            case CheckerPacket ignored -> new CheckerPacketReader();
            case IpAddressPacket ignored -> new IPReader();
            case IDPacket ignored -> new IDReader();
            case DestinationPacket ignored -> new DestinationPacketReader();
            case ResponsePacket ignored -> new ResponsePacketReader();
            case RangePacket ignored -> new RangePacketReader();
            case StringPacket ignored -> new StringReader();
            default -> throw new IllegalStateException("Unexpected value: " + record);
        };
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

    public static MultiReader reader(Record record, OpCodes opCode){
        if(!record.getClass().isRecord()){
            return null;
        }
        var list = CACHE
                .get(record.getClass());
        return new MultiReader(opCode, list.stream().map(fun -> fun.apply(record)).toArray(Reader[]::new));
    }
}
