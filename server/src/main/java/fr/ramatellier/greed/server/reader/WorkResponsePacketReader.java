package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.IDPacket;
import fr.ramatellier.greed.server.packet.ResponsePacket;

public class WorkResponsePacketReader {
    enum State {
        ERROR,
        WAITING_SRC_ID,
        WAITING_DST_ID,
        WAITING_REQUEST_ID,
        WAITING_RESPONSE,
    }
    private IDPacket src;
    private IDPacket dst;
    private Long requestID;
    private ResponsePacket responsePacket;
}
