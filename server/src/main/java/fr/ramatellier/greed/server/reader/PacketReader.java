package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.reader.full.*;
import fr.ramatellier.greed.server.reader.primitive.ByteReader;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.nio.ByteBuffer;

import static fr.ramatellier.greed.server.util.OpCodes.LOGOUT_DENIED;
import static fr.ramatellier.greed.server.util.OpCodes.LOGOUT_GRANTED;

public class PacketReader implements FullPacketReader {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private State state = State.WAITING_LOCATION;

    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();
    private final ConnectPacketReader connectPacketReader = new ConnectPacketReader();
    private final ConnectOKPacketReader connectOKPacketReader = new ConnectOKPacketReader();
    private final AddNodePacketReader addNodePacketReader = new AddNodePacketReader();
    private final WorkRequestPacketReader workRequestPacketReader = new WorkRequestPacketReader();
    private final WorkAssignmentPacketReader workAssignmentPacketReader = new WorkAssignmentPacketReader();
    private final WorkRequestResponseReader workRequestResponsePacketReader = new WorkRequestResponseReader();
    private final LogoutRequestPacketReader logoutRequestPacketReader = new LogoutRequestPacketReader();
    private final PleaseReconnectPacketReader pleaseReconnectPacketReader = new PleaseReconnectPacketReader();
    private final ReconnectPacketReader reconnectPacketReader = new ReconnectPacketReader();
    private final DisconnectedPacketReader disconnectedPacketReader = new DisconnectedPacketReader();
    private final WorkResponsePacketReader workResponsePacketReader = new WorkResponsePacketReader();
    private FullPacket value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(state == State.WAITING_LOCATION) {
            var status = locationReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_CODE;
            }
        }
        if(state == State.WAITING_CODE) {
            var status = codeReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                state = State.WAITING_PACKET;
            }
        }
        if(state == State.WAITING_PACKET) {
            var tramKind = TramKind.toTramKind(locationReader.get());
            if (tramKind == null) return ProcessStatus.ERROR;
            var opcodeValue = codeReader.get();
            var opcode = OpCodes.fromByte(opcodeValue);
            if (opcode == null) return ProcessStatus.ERROR;
            if (tramKind == TramKind.LOCAL) {
                LocalPacket valueTmp = switch(opcode){
                    case LOGOUT_GRANTED  ->
                        new LogoutGrantedPacket();
                    case LOGOUT_DENIED ->
                        new LogoutDeniedPacket();
                    case KO ->
                        new ConnectKOPacket();
                    default ->
                        null;
                };
                if (valueTmp != null) {
                    state = State.DONE;
                    value = valueTmp;
                    return ProcessStatus.DONE;
                }
                /*if (opcodeValue == LOGOUT_DENIED.BYTES) {
                    state = State.DONE;
                    value = new LogoutDeniedPacket();
                    return ProcessStatus.DONE;
                } else if (opcodeValue == LOGOUT_GRANTED.BYTES) {
                    state = State.DONE;
                    value = new LogoutGrantedPacket();
                    return ProcessStatus.DONE;
                } else if (codeReader.get() == OpCodes.KO.BYTES) {
                    state = State.DONE;
                    value = new ConnectKOPacket();
                    return ProcessStatus.DONE;
                }*/
            }
            var reader = fromOpCode(opcode);
            if (reader == null) return ProcessStatus.ERROR;

            var status = reader.process(buffer);
            if (status == ProcessStatus.DONE) {
                state = State.DONE;
                value = reader.get();
            }
        }
        if (state != State.DONE) {
            return ProcessStatus.REFILL;
        }

        return Reader.ProcessStatus.DONE;
    }


    @Override
    public FullPacket get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING_LOCATION;
        locationReader.reset();
        codeReader.reset();
        connectPacketReader.reset();
        connectOKPacketReader.reset();
        addNodePacketReader.reset();
        workRequestPacketReader.reset();
        workAssignmentPacketReader.reset();
        logoutRequestPacketReader.reset();
        pleaseReconnectPacketReader.reset();
        workRequestResponsePacketReader.reset();
        disconnectedPacketReader.reset();
        reconnectPacketReader.reset();
        workResponsePacketReader.reset();
    }
    private FullPacketReader fromOpCode(OpCodes opcode){
        return switch(opcode){
            case CONNECT -> connectPacketReader;
            case OK -> connectOKPacketReader;
            case ADD_NODE -> addNodePacketReader;
            case WORK -> workRequestPacketReader;
            case WORK_ASSIGNMENT -> workAssignmentPacketReader;
            case LOGOUT_REQUEST -> logoutRequestPacketReader;
            case PLEASE_RECONNECT -> pleaseReconnectPacketReader;
            case WORK_REQUEST_RESPONSE -> workRequestResponsePacketReader;
            case DISCONNECTED -> disconnectedPacketReader;
            case RECONNECT -> reconnectPacketReader;
            case WORK_RESPONSE -> workResponsePacketReader;
            default -> null;
        };
    }
}
