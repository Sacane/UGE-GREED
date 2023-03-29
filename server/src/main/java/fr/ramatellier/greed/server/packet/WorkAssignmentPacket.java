package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

public final class WorkAssignmentPacket implements FullPacket {
    private final IDPacket idSrc;
    private final IDPacket idDst;
    private final long requestId;
    private final List<RangePacket> ranges;

    public WorkAssignmentPacket(InetSocketAddress src, InetSocketAddress dst, long requestId, List<RangePacket> ranges) {
        idSrc = new IDPacket(src);
        idDst = new IDPacket(dst);
        this.requestId = requestId;
        this.ranges = ranges;
    }

    @Override
    public TramKind kind() {
        return TramKind.TRANSFERT;
    }

    @Override
    public byte opCode() {
        return OpCodes.WORK_ASSIGNEMENT.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idSrc.putInBuffer(buffer);
        idDst.putInBuffer(buffer);
        buffer.putLong(requestId);
        buffer.putInt(ranges.size());
        for(var range: ranges) {
            range.putInBuffer(buffer);
        }
    }
}
