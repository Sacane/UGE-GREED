package fr.ramatellier.greed.server.frame;

public enum FrameKind {
    LOCAL((byte)0x00), TRANSFER((byte)0x01), BROADCAST((byte)0x02);

    public final byte BYTES;
    public static FrameKind of(byte value) {
        return switch(value){
            case 0x00 -> LOCAL;
            case 0x01 -> TRANSFER;
            case 0x02 -> BROADCAST;
            default -> null;
        };
    }
    FrameKind(byte value) {
        this.BYTES = value;
    }
}
