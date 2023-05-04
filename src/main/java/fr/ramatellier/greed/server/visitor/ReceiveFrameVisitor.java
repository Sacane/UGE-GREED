package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.SharingProcessExecutor;
import fr.ramatellier.greed.server.compute.SocketUcIdentifier;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.model.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ReceiveFrameVisitor implements FrameVisitor {
    private final Server server;
    private final Context context;
    private static final Logger logger = Logger.getLogger(ReceiveFrameVisitor.class.getName());

    public ReceiveFrameVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void visit(ConnectFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        logger.info("Connection demand received from " + packet.idPacket().getSocket() + " " + packet.idPacket().getPort());
        if(server.isRunning()) {
            logger.info("Connection accepted");
            var list = new IDListComponent(server.registeredAddresses().stream().map(IDComponent::new).toList());
            System.out.println("List of neighbors: " + list);
            var response = new ConnectOKFrame(new IDComponent(server.getAddress()),
                    list);
            context.queuePacket(response);
            InetSocketAddress socket = packet.idPacket().getSocket();
            server.addRoot(socket, socket, context);
            var addNodePacket = new AddNodeFrame(new IDComponent(server.getAddress()), new IDComponent(socket));
            server.broadcast(addNodePacket, socket);
        }
        else {
            logger.info("Connection refused");
            context.queuePacket(new ConnectKOFrame());
        }
    }

    @Override
    public void visit(WorkResponseFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        var responsePacket = packet.responsePacket();

        switch(responsePacket.getResponseCode()) {
            case 0x00 -> System.out.println(responsePacket.getResponse().value());
            case 0x01 -> System.out.println("An exception has occur while computing the value : " + responsePacket.getValue());
            case 0x02 -> System.out.println("Time out while computing the value : " + responsePacket.getValue());
            case 0x03 -> System.out.println("Cannot get the checker");
            default -> logger.severe("UNKNOWN RESPONSE CODE");
        }

        var id = new ComputationIdentifier(packet.requestID().get(), server.getAddress());
        try {
            server.treatComputationResult(id, packet.result());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "The file cannot be written : ", e);
        }
    }

    @Override
    public void visit(LogoutRequestFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        if(server.isRunning()) {
            context.queuePacket(new LogoutGrantedFrame());
            if(packet.daughters().sizeList() == 0) {
                server.broadcast(new DisconnectedFrame(new IDComponent(server.getAddress()), packet.id()), server.getAddress());
                server.deleteAddress(packet.id().getSocket());
            }
            else {
                server.newLogoutRequest(packet.id().getSocket(), packet.daughters().idPacketList().stream().map(IDComponent::getSocket).toList());
            }
        }
        else {
            context.queuePacket(new LogoutDeniedFrame());
        }
    }

    @Override
    public void visit(ReconnectFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        server.receiveReconnect(packet.id().getSocket());
        server.addRoot(packet.id().getSocket(), packet.id().getSocket(), context);

        for(var id: packet.ancestors().idPacketList()) {
            server.addRoot(id.getSocket(), packet.id().getSocket(), context);
        }

        if(server.allConnected()) {
            server.broadcast(new DisconnectedFrame(new IDComponent(server.getAddress()), new IDComponent(server.getAddressLogout())), server.getAddress());
            server.deleteAddress(server.getAddressLogout());

            if(server.isShutdown()) {
                server.sendLogout();
            }
        }
    }



    @Override
    public void visit(WorkRequestResponseFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        if(packet.nb_uc().get() == 0){
            return;
        }
        var computeId = new ComputationIdentifier(packet.requestID().get(), server.getAddress());
        var entity = server.retrieveWaitingComputation(computeId);
        if(entity == null){
            return;
        }
        server.incrementWaitingWorker(computeId);
        server.storeComputation(computeId, new SocketUcIdentifier(packet.src().getSocket(), packet.nb_uc().get()));
        if(server.isRoomReady(computeId)){
            var process = new SharingProcessExecutor(
                    server.availableSocketsUc(computeId),
                    entity.info().end() - entity.info().start()
            );
            var socketRangeList = process.shareAndGet(entity.info().start());
            for(var socketRange: socketRangeList){
                var workAssignmentPacket = new WorkAssignmentFrame(
                        new IDComponent(server.getAddress()),
                        new IDComponent(socketRange.socketAddress()),
                        packet.requestID(),
                        new RangeComponent(socketRange.range().start(), socketRange.range().end())
                );
                server.transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }
}
