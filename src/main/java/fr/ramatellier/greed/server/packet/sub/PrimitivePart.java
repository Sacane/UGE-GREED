package fr.ramatellier.greed.server.packet.sub;

public abstract sealed class PrimitivePart<T> permits BytePacketPart, IntPacketPart, LongPacketPart {
    private final T value;
    public PrimitivePart(T value) {
        this.value = value;
    }
    public T get(){
        return value;
    }
}
