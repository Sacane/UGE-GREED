package fr.ramatellier.greed.server.util;

public enum OpCodes {

    CONNECT((byte)0x01), KO((byte)0x02), OK((byte)0x03), ADD_NODE((byte)0x04), WORK((byte)0x05), WORK_ASSIGNEMENT((byte) 0x06), WORK_RESPONSE((byte)0x07), WORK_REQUEST_RESPONSE((byte)0x08), LOGOUT_REQUEST((byte)0x10), LOGOUT_GRANTED((byte)0x12);
    public final byte BYTES;

    OpCodes(byte bytes) {
        this.BYTES = bytes;
    }
    public static OpCodes fromByte(byte b){
        for(OpCodes opCode : OpCodes.values()){
            if(opCode.BYTES == b){
                return opCode;
            }
        }
        return null;
    }
}
