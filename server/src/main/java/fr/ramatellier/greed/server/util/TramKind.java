package fr.ramatellier.greed.server.util;

public enum TramKind {
    LOCAL((byte)0x00), TRANSFER((byte)0x01), BROADCAST((byte)0x02);

    public final byte BYTES;
    public static TramKind toTramKind(byte value) {
        for (TramKind tramKind : values()) {
            if (tramKind.BYTES == value) {
                return tramKind;
            }
        }
        return null;
    }
    TramKind(byte value) {
        this.BYTES = value;
    }
}
