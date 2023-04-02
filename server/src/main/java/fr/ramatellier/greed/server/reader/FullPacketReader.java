package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.FullPacket;
import fr.ramatellier.greed.server.reader.full.*;
import fr.ramatellier.greed.server.util.OpCodes;


public interface FullPacketReader extends Reader<FullPacket> {
    static FullPacketReader fromOpCode(OpCodes opcode){
        return switch (opcode) {
            case ADD_NODE -> new AddNodePacketReader();
            case CONNECT -> new ConnectPacketReader();
            case OK -> new ConnectOKPacketReader();
            case DISCONNECTED -> new DisconnectedPacketReader();
            case LOGOUT_REQUEST -> new LogoutRequestPacketReader();
            case PLEASE_RECONNECT -> new PleaseReconnectPacketReader();
            case RECONNECT -> new ReconnectPacketReader();
            case WORK_ASSIGNMENT -> new WorkAssignmentPacketReader();
            case WORK -> new WorkRequestPacketReader();
            case WORK_REQUEST_RESPONSE -> new WorkRequestResponseReader();
            case WORK_RESPONSE -> new WorkResponsePacketReader();
            case KO -> new ConnectKOPacketReader();
            case LOGOUT_DENIED -> new LogoutDeniedPacketReader();
            case LOGOUT_GRANTED -> new LogoutGrantedPacketReader();
        };
    }
}
