package fr.ramatellier.greed.server.packet.sub;

public non-sealed class BytePacketPart extends PrimitivePart<Byte> implements Part {
    public BytePacketPart(Byte value) {
        super(value);
    }
}
