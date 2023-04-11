package fr.ramatellier.greed.server.packet.full;

import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.sub.CheckerPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.util.OpCodes;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class WorkRequestPacket implements TransferPacket {
    private final IDPacket idSrc;
    private final IDPacket idDst;
    private final long requestId;
    private final CheckerPacket checker;
    private final RangePacket range;
    private final long max;

    public WorkRequestPacket(InetSocketAddress src, InetSocketAddress dst, long requestId, String url, String className, long start, long end, long max) {
        idSrc = new IDPacket(src);
        idDst = new IDPacket(dst);
        this.requestId = requestId;
        checker = new CheckerPacket(url, className);
        range = new RangePacket(start, end);
        this.max = max;
    }

    public IDPacket src() {
        return idSrc;
    }

    public IDPacket dst() {
        return idDst;
    }

    public long getRequestId() {
        return requestId;
    }

    public CheckerPacket getChecker() {
        return checker;
    }

    public RangePacket getRange() {
        return range;
    }

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK;
    }

    @Override
    public void put(ByteBuffer buffer) {
        idSrc.putInBuffer(buffer);
        idDst.putInBuffer(buffer);
        buffer.putLong(requestId);
        checker.putInBuffer(buffer);
        range.putInBuffer(buffer);
        buffer.putLong(max);
    }

    @Override
    public int size() {
        return Byte.BYTES * 2 + idSrc.size() + idDst.size() + Long.BYTES * 2 + checker.size() + range.size();
    }
}
