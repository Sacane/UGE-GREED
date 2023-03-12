package fr.ramatellier.greed.server.util;

public enum TramKind {
    LOCAL((byte)0x00), TRANSFERT((byte)0x01), BROADCAST((byte)0x02), TO_LOCAL((byte)0x03), ANY((byte)0x00);
    public final byte BYTES;

    TramKind(byte value) {
        this.BYTES = value;
    }
}
