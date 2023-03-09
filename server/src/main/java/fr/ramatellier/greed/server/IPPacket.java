package fr.ramatellier.greed.server;

import java.nio.ByteBuffer;

public class IPPacket implements Packet {
    private final byte size;
    private final String address;

    public IPPacket(String address) {
        if(address.contains(".")) {
            size = 4;
        }
        else {
            size = 6;
        }

        this.address = address;
    }

    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(size);

        if(size == 4) {
            var values = address.split(".");

            for(var value: values) {
                buffer.put(Byte.valueOf(value));
            }
        }
        else {
            var values = address.split(":");

            for(var value: values) {
                buffer.putShort(Short.valueOf(value));
            }
        }
    }
}
