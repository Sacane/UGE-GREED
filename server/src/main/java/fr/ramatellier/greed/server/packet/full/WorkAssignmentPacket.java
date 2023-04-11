package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.compute.Range;
import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class WorkAssignmentPacket implements TransferPacket {
    private final IDPacket idSrc;
    private final IDPacket idDst;
    private final long requestId;
    private final RangePacket range;

    public WorkAssignmentPacket(InetSocketAddress src, InetSocketAddress dst, long requestId, Range range) {
        idSrc = new IDPacket(src);
        idDst = new IDPacket(dst);
        this.requestId = requestId;
        this.range = new RangePacket(range.start(), range.end());
    }

    @Override
    public IDPacket dst() {
        return idDst;
    }

    public long getRequestId() {
        return requestId;
    }

    @Override
    public IDPacket src() {
        return idSrc;
    }

    public Range getRanges() {
        return new Range(range.start(), range.end());
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_ASSIGNMENT;
    }

    @Override
    public void put(ByteBuffer buffer) {
        idSrc.putInBuffer(buffer);
        idDst.putInBuffer(buffer);
        buffer.putLong(requestId);
        range.putInBuffer(buffer);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + idSrc.size() + idDst.size() + Long.BYTES + range.size();
    }
}
