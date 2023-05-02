package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.frame.model.*;

import java.util.function.Predicate;

public enum OpCodes {
    CONNECT((byte)0x01, ConnectFrame.class),
    KO((byte)0x02, ConnectKOFrame.class),
    OK((byte)0x03, ConnectOKFrame.class),
    ADD_NODE((byte)0x04, AddNodeFrame.class),
    WORK((byte)0x05, WorkRequestFrame.class),
    WORK_ASSIGNMENT((byte) 0x06, WorkAssignmentFrame.class),
    WORK_RESPONSE((byte)0x07, WorkResponseFrame.class),
    WORK_REQUEST_RESPONSE((byte)0x08, WorkRequestResponseFrame.class),
    LOGOUT_REQUEST((byte)0x10, LogoutRequestFrame.class),
    LOGOUT_DENIED((byte)0x11, LogoutDeniedFrame.class),
    LOGOUT_GRANTED((byte)0x12, LogoutGrantedFrame.class),
    PLEASE_RECONNECT((byte)0x13, PleaseReconnectFrame.class),
    RECONNECT((byte)0x14, ReconnectFrame.class),
    DISCONNECTED((byte)0x15, DisconnectedFrame.class);

    public final byte BYTES;
    public final Class<? extends Frame> frameClass;

    OpCodes(byte bytes, Class<? extends Frame> frameClass) {
        this.BYTES = bytes;
        this.frameClass = frameClass;
    }

    public static OpCodes fromByte(byte value){
        return retrieveIf(opCode -> opCode.BYTES == value);
    }
    public static OpCodes fromFrame(Frame frame){
        return retrieveIf(opCode -> opCode.frameClass.equals(frame.getClass()));
    }

    private static OpCodes retrieveIf(Predicate<OpCodes> predicate){
        for (OpCodes opCode : values()) {
            if (predicate.test(opCode)) {
                return opCode;
            }
        }
        return null;
    }
}
