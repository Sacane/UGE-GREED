package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.*;
import java.util.Objects;
import java.util.logging.Logger;

public class ServerVisitor implements PacketVisitor {

    private final Server server;
    private final Context context;
    private static final Logger logger = Logger.getLogger(ServerVisitor.class.getName());

    public ServerVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    public Context getContext(){
        return context;
    }

    @Override
    public void visit(ConnectPacket packet) {

        //send OKPacket
        if(server.isRunning()){
            logger.info("Connection demand received from " + packet.getAddress() + " " + packet.getPort());

            var response = new ConnectOKPacket(server.getAddress(), server.neighbours());
            context.queuePacket(response);

            var socket = packet.getSocket();
            server.addRoot(socket, socket);
        }

    }

    @Override
    public void visit(ConnectOKPacket packet) {
        logger.info("Connection accepted from " + packet.getAddress() + " on port " + packet.getPort());

        var addressMother = packet.getMotherAddress();
        for(var neighbor: packet.neighbours()) {
            server.addRoot(neighbor, addressMother);
        }
        server.addRoot(addressMother, addressMother);
    }

    @Override
    public void visit(ConnectKOPacket packet) {
        System.out.println("ConnectKOPacket");
    }

    private void queuePacket(FullPacket packet){
        switch (packet.kind()) {
            case BROADCAST -> queueBroadcastPacket(packet);
            case LOCAL -> queueLocalPacket(packet);
            case TRANSFERT -> queueTransferPacket(packet);
        }
    }

    private void queueBroadcastPacket(FullPacket packet){
        //Broadcast this packet to all neighbours
        server.broadcast(packet);
    }
    private void queueLocalPacket(FullPacket packet){
        //DO nothing special except treat the packet
        context.queuePacket(packet);
    }
    private void queueTransferPacket(FullPacket packet){
        server.transfer(packet);
    }

}
