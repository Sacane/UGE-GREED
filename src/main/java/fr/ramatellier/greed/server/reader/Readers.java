package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.frame.model.Frame;
import fr.ramatellier.greed.server.util.OpCode;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Readers {
    private static final ClassValue<Function<? super Frame, PacketComponents>> CACHE = new ClassValue<>() {
        @Override
        protected Function<? super Frame, PacketComponents> computeValue(Class<?> type) {
            var map = initFrameReader();
            return frame -> map.get(OpCode.fromFrame(frame));
        }
    };

    private static Map<OpCode, PacketComponents> initFrameReader() {
        var opCodeToConstructor = new HashMap<OpCode, PacketComponents>();
        for(var opcode: OpCode.values()) {
            var record = opcode.frameClass;
            if (record == null || !record.isRecord())
                throw new IllegalArgumentException("OpCode " + opcode + " is not a record");
            var fields = record.getRecordComponents();
            var components = Arrays.stream(fields).map(RecordComponent::getType).toArray(Class<?>[]::new);
            opCodeToConstructor.put(opcode, new PacketComponents(record, components));
        }
        return opCodeToConstructor;
    }

    public static PacketComponents getPacketComponents(Frame frame) {
        return CACHE.get(frame.getClass()).apply(frame);
    }
}
