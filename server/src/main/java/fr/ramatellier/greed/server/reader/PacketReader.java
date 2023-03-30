package fr.ramatellier.greed.server.reader;

import fr.ramatellier.greed.server.packet.*;
import fr.ramatellier.greed.server.util.OpCodes;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class PacketReader implements Reader<FullPacket> {
    private enum State {
        DONE, WAITING_LOCATION, WAITING_CODE, WAITING_PACKET, ERROR
    }
    private State state = State.WAITING_LOCATION;
    private final ByteReader locationReader = new ByteReader();
    private final ByteReader codeReader = new ByteReader();
    private final IDReader idReader = new IDReader();
    private final ConnectOKPacketReader connectOKPacketReader = new ConnectOKPacketReader();
    private final AddNodePacketReader addNodePacketReader = new AddNodePacketReader();
    private final WorkRequestPacketReader workRequestPacketReader = new WorkRequestPacketReader();
    private final WorkAssignmentPacketReader workAssignmentPacketReader = new WorkAssignmentPacketReader();
    private final WorkRequestResponseReader workRequestResponsePacketReader = new WorkRequestResponseReader();
    private final LogoutRequestPacketReader logoutRequestPacketReader = new LogoutRequestPacketReader();
    private final PleaseReconnectPacketReader pleaseReconnectPacketReader = new PleaseReconnectPacketReader();
    private final ReconnectPacketReader reconnectPacketReader = new ReconnectPacketReader();
    private final DisconnectedPacketReader disconnectedPacketReader = new DisconnectedPacketReader();
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
            if(locationReader.get() == TramKind.LOCAL.BYTES) {
                if(codeReader.get() == OpCodes.CONNECT.BYTES) {
                    var status = idReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        var packet = idReader.get();
                        value = new ConnectPacket(new InetSocketAddress(packet.getHostname(), packet.getPort()));
                        state = State.DONE;
                    }
                }
                else if(codeReader.get() == OpCodes.OK.BYTES) {
                    var status = connectOKPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = connectOKPacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.KO.BYTES) {
                    state = State.DONE;

                    value = new ConnectKOPacket();
                }
                else if(codeReader.get() == OpCodes.LOGOUT_REQUEST.BYTES) {
                    var status = logoutRequestPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = logoutRequestPacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.LOGOUT_DENIED.BYTES) {
                    state = State.DONE;

                    value = new LogoutDeniedPacket();
                }
                else if(codeReader.get() == OpCodes.LOGOUT_GRANTED.BYTES) {
                    state = State.DONE;

                    value = new LogoutGrantedPacket();
                }
                else if(codeReader.get() == OpCodes.PLEASE_RECONNECT.BYTES) {
                    var status = pleaseReconnectPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = pleaseReconnectPacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.RECONNECT.BYTES) {
                    var status = reconnectPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = reconnectPacketReader.get();
                    }
                }
            }
            else if(locationReader.get() == TramKind.BROADCAST.BYTES) {
                if(codeReader.get() == OpCodes.ADD_NODE.BYTES) {
                    var status = addNodePacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = addNodePacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.DISCONNECTED.BYTES) {
                    var status = disconnectedPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = disconnectedPacketReader.get();
                    }
                }
            }
            else if(locationReader.get() == TramKind.TRANSFERT.BYTES) {
                if(codeReader.get() == OpCodes.WORK.BYTES) {
                    var status = workRequestPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = workRequestPacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.WORK_ASSIGNEMENT.BYTES) {
                    var status = workAssignmentPacketReader.process(buffer);

                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;

                        value = workRequestPacketReader.get();
                    }
                }
                else if(codeReader.get() == OpCodes.WORK_REQUEST_RESPONSE.BYTES){
                    var status = workRequestResponsePacketReader.process(buffer);
                    if(status == ProcessStatus.DONE) {
                        state = State.DONE;
                        value = workRequestResponsePacketReader.get();
                    }
                }
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
        idReader.reset();
        connectOKPacketReader.reset();
        addNodePacketReader.reset();
        workRequestPacketReader.reset();
        workAssignmentPacketReader.reset();
        logoutRequestPacketReader.reset();
        pleaseReconnectPacketReader.reset();
        workRequestResponsePacketReader.reset();
        disconnectedPacketReader.reset();
        reconnectPacketReader.reset();
    }
}
