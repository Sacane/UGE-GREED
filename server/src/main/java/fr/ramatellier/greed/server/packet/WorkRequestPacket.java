package fr.ramatellier.greed.server.packet;

import fr.ramatellier.greed.server.compute.ComputationEntity;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.Range;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public final class WorkRequestPacket implements FullPacket {
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

    public IDPacket getIdSrc() {
        return idSrc;
    }

    public IDPacket getIdDst() {
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

    public long getMax() {
        return max;
    }

    @Override
    public TramKind kind() {
        return TramKind.TRANSFERT;
    }

    @Override
    public byte opCode() {
        return OpCodes.WORK.BYTES;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        putHeader(buffer);
        idSrc.putInBuffer(buffer);
        idDst.putInBuffer(buffer);
        buffer.putLong(requestId);
        checker.putInBuffer(buffer);
        range.putInBuffer(buffer);
        buffer.putLong(max);
    }
    public ComputationEntity toComputationEntity(){
        return new ComputationEntity(new ComputationIdentifier(requestId, idSrc.getSocket().getHostName()), new Range(range.start(), range.end()), checker.getClassName(), idDst.getAddress(), checker.getUrl());
    }
}
