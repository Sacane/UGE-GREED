package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.util.http.NonBlockingHTTPJarProvider;
import fr.uge.ugegreed.Checker;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.LongStream;

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
    public void visit(ConnectOKFrame packet) {
        logger.info("Connection accepted from " + packet.idMother().getSocket() + " on port " + packet.idMother().getPort());
        var addressMother = packet.idMother();
        server.updateParentAddress(addressMother.getSocket());
        for(var neighbor: packet.neighbours().idPacketList()) {
            System.out.println("Add neighbor " + neighbor.getSocket() + " to root table");
            server.addRoot(neighbor.getSocket(), addressMother.getSocket(), context);
        }
        server.addRoot(addressMother.getSocket(), addressMother.getSocket(), context);
    }

    @Override
    public void visit(ConnectKOFrame packet) {
        System.out.println("Connection refused, target server is shutting down...");
        server.shutdown();
    }

    @Override
    public void visit(AddNodeFrame packet) {
        logger.info("AddNodePacket received from " + packet.src().getSocket());
        server.addRoot(packet.daughter().getSocket(), packet.src().getSocket(), context);
        logger.info("update root table and send broadcast to neighbours");
    }

    @Override
    public void visit(WorkRequestFrame packet) {
        if(server.isRunning()) {
            var deltaComputingPossibility = Server.MAXIMUM_COMPUTATION - server.currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server.addRoom(new ComputationEntity(new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket()),
                        new ComputeInfo(packet.checker().url(), packet.checker().className(), packet.range().start(), packet.range().end())));
                server.transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
                        packet.src(),
                        packet.dst(),
                        packet.requestId(),
                        LongComponent.of(deltaComputingPossibility)
                ));
            }
        }
        else {
            server.transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
                    packet.src(),
                    packet.dst(),
                    packet.requestId(),
                    LongComponent.of(0L)
            ));
        }
    }

    /**
     * In case we receive a workResponsePacket, we check if we are the destination of the packet.
     * If so, we just print the result of the computation.
     * @param packet the packet to visit
     */
    @Override
    public void visit(WorkAssignmentFrame packet) {
        System.out.println("Start computation...");
        var idContext = new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket());
        server.updateRoom(idContext, packet.range().start(), packet.range().end());
        var entityResponse = server.findComputationById(idContext);
        if(entityResponse.isEmpty()) {
            return ;
        }
        var entity = entityResponse.get();
        var targetRange = packet.range();

        // HTTP non-blocking
        try {
            var httpClient = NonBlockingHTTPJarProvider.fromURL(new URL(entity.info().url()));
            httpClient.onDone(body -> {
                Checker checker = Server.retrieveChecker(httpClient, entity.info().className());
                for(var i = targetRange.start(); i < targetRange.end(); i++) {
                    server.addTask(new TaskComputation(packet, checker, entity.id(), i));
                }
            });
            httpClient.launch();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            logger.severe("CANNOT GET THE CHECKER");
            LongStream.range(targetRange.start(), targetRange.end()).forEach(i -> sendResponseWithOPCode(packet, i, "CANNOT GET THE CHECKER", (byte) 0x03));
        }
    }

    private void sendResponseWithOPCode(WorkAssignmentFrame origin, long index, String result, byte opcode) {
        server.transfer(origin.src().getSocket(), new WorkResponseFrame(
                origin.dst(),
                origin.src(),
                origin.requestId(),
                new ResponseComponent(index, result, opcode)
        ));
    }

    @Override
    public void visit(WorkResponseFrame packet) {
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
    public void visit(LogoutDeniedFrame packet) {
        System.out.println("LOGOUT DENIED");
    }

    @Override
    public void visit(LogoutGrantedFrame packet) {
        System.out.println("LOGOUT GRANTED");
        var daughtersContext = server.daughtersContext();

        for(var daughterContext: daughtersContext) {
            daughterContext.queuePacket(new PleaseReconnectFrame(new IDComponent(server.getParentSocketAddress())));
        }
    }

    @Override
    public void visit(PleaseReconnectFrame packet) {
        try {
            server.connectToNewParent(packet.id());
        } catch (IOException e) {
            // Ignore exception
        }
    }

    @Override
    public void visit(ReconnectFrame packet) {
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
    public void visit(DisconnectedFrame packet) {
        if(server.getAddress().equals(packet.id().getSocket())) {
            server.shutdown();
        }
        else {
            server.deleteAddress(packet.id().getSocket());

            if(server.isShutdown() && packet.id().getSocket().equals(server.getParentSocketAddress())) {
                server.sendLogout();
            }
        }
    }

    @Override
    public void visit(WorkRequestResponseFrame packet) {
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
