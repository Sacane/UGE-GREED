package fr.ramatellier.greed.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPPacket implements Packet {
    private final byte ipCode; // 0x04 for IPV4, 0x6 for IPV6
    private final String address;

    public IPPacket(String address) {
        if(address.contains(".")) {
            this.ipCode = 0x04;
        }
        else {
            this.ipCode = 0x06;
        }

        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void putInBuffer(ByteBuffer buffer) {
        buffer.put(ipCode);
        try {
            var ipAddress = InetAddress.getByName(address).getAddress();
            buffer.put(ipAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        if(ipCode == 0x04) {
//
//            var values = address.split(".");
//
//            for(var value: values) {
//                buffer.put(Byte.parseByte(value));
//            }
//        }
//        else {
//            var values = address.split(":");
//
//            for(var value: values) {
//                buffer.putShort(Short.parseShort(value));
//            }
//        }
    }

}
