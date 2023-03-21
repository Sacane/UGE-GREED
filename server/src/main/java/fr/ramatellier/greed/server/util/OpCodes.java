package fr.ramatellier.greed.server.util;

public final class OpCodes {
    private OpCodes() {}

    public static final byte CONNECT = 0x01;
    public static final byte KO = 0x02;
    public static final byte OK = 0x03;
    public static final byte ADD_NODE = 0x04;
}
