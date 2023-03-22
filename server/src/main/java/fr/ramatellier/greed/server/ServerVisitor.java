package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.*;
import fr.ramatellier.greed.server.util.TramKind;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ServerVisitor implements PacketVisitor {
    private final Server server;
    private final Context context;
    private static final Logger logger = Logger.getLogger(ServerVisitor.class.getName());

    public ServerVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void visit(ConnectPacket packet) {
        if(server.isRunning()) {
            logger.info("Connection demand received from " + packet.getAddress() + " " + packet.getPort());
            var response = new ConnectOKPacket(server.getAddress(), server.registeredAddresses());
            context.queuePacket(response);
            var socket = packet.getSocket();
            server.addRoot(socket, socket, context);

            var addNodePacket = new AddNodePacket(new IDPacket(server.getAddress()), new IDPacket(socket));
            queueBroadcastPacket(addNodePacket, socket);
        }
        //TODO send KOPacket if server is not running
    }

    @Override
    public void visit(ConnectOKPacket packet) {
        logger.info("Connection accepted from " + packet.getAddress() + " on port " + packet.getPort());
        var addressMother = packet.getMotherAddress();
        for(var neighbor: packet.neighbours()) {
            server.addRoot(neighbor, addressMother, context);
        }
        server.addRoot(addressMother, addressMother, context);
    }

    @Override
    public void visit(ConnectKOPacket packet) {
        System.out.println("ConnectKOPacket");
    }

    @Override
    public void visit(AddNodePacket packet) {
        logger.info("AddNodePacket received from " + packet.getSrc().getSocket());
        server.addRoot(packet.getDaughter().getSocket(), packet.getSrc().getSocket(), context);

        logger.info("update root table and send broadcast to neighbours");
        var addNodePacket = new AddNodePacket(new IDPacket(server.getAddress()), packet.getDaughter());
        queueBroadcastPacket(addNodePacket, packet.getSrc().getSocket());
    }

    @Override
    public void visit(WorkRequestPacket packet) {
    }

    //Broadcast this packet to all neighbours
    private void queueBroadcastPacket(FullPacket packet, InetSocketAddress address) {
        if(packet.kind() != TramKind.BROADCAST){
            throw new AssertionError();
        }

        server.broadcast(packet, address);
    }
    private void queueLocalPacket(FullPacket packet){
        //DO nothing special except treat the packet
        context.queuePacket(packet);
    }
    private void queueTransferPacket(FullPacket packet){
        server.transfer(context.src(), packet);
    }

}
