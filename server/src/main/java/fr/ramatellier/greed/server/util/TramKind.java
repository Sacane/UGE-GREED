package fr.ramatellier.greed.server.util;

public enum TramKind {
    LOCAL((byte)0x00), TRANSFER((byte)0x01), BROADCAST((byte)0x02);

    public final byte BYTES;

    TramKind(byte value) {
        this.BYTES = value;
    }
}
