package fr.ramatellier.greed.server.visitor;


import fr.ramatellier.greed.server.compute.ComputeInfo;
import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.compute.ComputationEntity;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.SharingProcessExecutor;
import fr.ramatellier.greed.server.compute.SocketUcIdentifier;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.LongStream;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ReceivePacketVisitor implements PacketVisitor {
    private final Server server;
    private final Context context;
    private static final Logger logger = Logger.getLogger(ReceivePacketVisitor.class.getName());

    public ReceivePacketVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void visit(ConnectPacket packet) {
        logger.info("Connection demand received from " + packet.getAddress() + " " + packet.getPort());
        if(server.isRunning()) {
            logger.info("Connection accepted");
            var response = new ConnectOKPacket(server.getAddress(), server.registeredAddresses());
            context.queuePacket(response);
            InetSocketAddress socket = packet.getSocket();
            server.addRoot(socket, socket, context);
            var addNodePacket = new AddNodePacket(new IDPacket(server.getAddress()), new IDPacket(socket));
            server.broadcast(addNodePacket, socket);
        }
        else {
            logger.info("Connection refused");
            context.queuePacket(new ConnectKOPacket());
        }
    }

    @Override
    public void visit(ConnectOKPacket packet) {
        logger.info("Connection accepted from " + packet.getAddress() + " on port " + packet.getPort());
        var addressMother = packet.getMotherAddress();
        server.updateParentAddress(addressMother);
        for(var neighbor: packet.neighbours()) {
            server.addRoot(neighbor, addressMother, context);
        }
        server.addRoot(addressMother, addressMother, context);
    }

    @Override
    public void visit(ConnectKOPacket packet) {
        System.out.println("Connection refused, target server is shutting down...");
        server.shutdown();
    }

    @Override
    public void visit(AddNodePacket packet) {
        logger.info("AddNodePacket received from " + packet.src().getSocket());
        server.addRoot(packet.daughter().getSocket(), packet.src().getSocket(), context);
        logger.info("update root table and send broadcast to neighbours");
    }

    @Override
    public void visit(WorkRequestPacket packet) {
        if(server.getAddress().equals(packet.dst().getSocket())) {
            var deltaComputingPossibility = Server.MAXIMUM_COMPUTATION - server.currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server.addRoom(new ComputationEntity(new ComputationIdentifier(packet.getRequestId(), packet.src().getSocket()),
                                new ComputeInfo(packet.getChecker().url(), packet.getChecker().className(), packet.getRange().start(), packet.getRange().end())));
                server.transfer(packet.src().getSocket(), new WorkRequestResponsePacket(
                        packet.src(),
                        packet.dst(),
                        packet.getRequestId(),
                        deltaComputingPossibility
                ));
            }
        }
        else {
            server.transfer(packet.dst().getSocket(), packet);
        }
    }

    /**
     * In case we receive a workResponsePacket, we check if we are the destination of the packet.
     * If so, we just print the result of the computation.
     * @param packet the packet to visit
     */
    @Override
    public void visit(WorkAssignmentPacket packet) {
        System.out.println("RECEIVED WORK ASSIGNMENT PACKET");
        var idContext = new ComputationIdentifier(packet.getRequestId(), packet.src().getSocket());
        var entityResponse = server.findComputationById(idContext);
        if(entityResponse.isEmpty()){
            return;
        }
        var entity = entityResponse.get();
        var targetRange = packet.getRanges();
        var result = Client.checkerFromHTTP(entity.info().url(), entity.info().className());
        if(result.isEmpty()){
            logger.severe("CANNOT GET THE CHECKER");
            LongStream.range(targetRange.start(), targetRange.end()).forEach(i -> sendResponseWithOPCode(packet, i, "CANNOT GET THE CHECKER", (byte) 0x03));
            return;
        }
        var checker = result.get();
        for(var i = targetRange.start(); i < targetRange.end(); i++){
            try{
                var checkerResult = checker.check(i);
                sendResponseWithOPCode(packet, i, checkerResult, (byte) 0x00);
            } catch (InterruptedException e) {
                logger.severe("INTERRUPTED EXCEPTION");
                sendResponseWithOPCode(packet, i, null, (byte) 0x01);
            }catch (Exception e){
                sendResponseWithOPCode(packet, i, null, (byte) 0x01);
            }
        }
    }
    private void sendResponseWithOPCode(WorkAssignmentPacket origin, long index,String result, byte opcode){
        server.transfer(origin.src().getSocket(), new WorkResponsePacket(
                origin.dst(),
                origin.src(),
                origin.getRequestId(),
                new ResponsePacket(index, result, opcode)
        ));
    }
    @Override
    public void visit(WorkResponsePacket packet) {
        var responsePacket = packet.responsePacket();
        switch(packet.responsePacket().getResponseCode()){
            //TODO Create file and fill response inside
            case 0x00 -> {
                System.out.println(responsePacket.getResponse().value());
            }
            case 0x01 -> System.out.println("An exception has occur while computing the value : " + responsePacket.getValue());
            case 0x02 -> System.out.println("Time out while computing the value : " + responsePacket.getValue());
            case 0x03 -> System.out.println("Cannot get the checker");
            default -> logger.severe("UNKNOWN RESPONSE CODE");
        }
    }

    @Override
    public void visit(LogoutRequestPacket packet) {
        if(server.isRunning()) {
            context.queuePacket(new LogoutGrantedPacket());
            if(packet.getDaughters().size() == 0) {
                server.broadcast(new DisconnectedPacket(server.getAddress(), packet.getId().getSocket()), server.getAddress());
                server.deleteAddress(packet.getId().getSocket());
            }
            else {
                server.newLogoutRequest(packet.getId().getSocket(), packet.getDaughters().stream().map(IDPacket::getSocket).toList());
            }
        }
        else {
            context.queuePacket(new LogoutDeniedPacket());
        }
    }

    @Override
    public void visit(LogoutDeniedPacket packet) {
        System.out.println("LOGOUT DENIED");
    }

    @Override
    public void visit(LogoutGrantedPacket packet) {
        var daughtersContext = server.daughtersContext();

        for(var daughterContext: daughtersContext) {
            daughterContext.queuePacket(new PleaseReconnectPacket(server.getParentSocketAddress()));
        }
    }

    @Override
    public void visit(PleaseReconnectPacket packet) {
        try {
            server.connectToNewParent(packet.getId());
        } catch (IOException e) {
            // Ignore exception
        }
    }

    @Override
    public void visit(ReconnectPacket packet) {
        server.receiveReconnect(packet.getId().getSocket());
        server.addRoot(packet.getId().getSocket(), packet.getId().getSocket(), context);

        for(var id: packet.getAncestors()) {
            server.addRoot(id.getSocket(), packet.getId().getSocket(), context);
        }

        if(server.allConnected()) {
            server.broadcast(new DisconnectedPacket(server.getAddress(), server.getAddressLogout()), server.getAddress());
            server.deleteAddress(server.getAddressLogout());
        }
    }

    @Override
    public void visit(DisconnectedPacket packet) {
        if(server.getAddress().equals(packet.id().getSocket())) {
            server.shutdown();
        }
        else {
            server.deleteAddress(packet.id().getSocket());
        }
    }

    @Override
    public void visit(WorkRequestResponsePacket packet) {
        if(packet.nb_uc() == 0){
            return;
        }
        var computeId = new ComputationIdentifier(packet.requestID(), server.getAddress());
        var entity = server.retrieveWaitingComputation(computeId);
        if(entity == null){
            return;
        }
        server.incrementWaitingWorker(computeId);
        server.storeComputation(computeId, new SocketUcIdentifier(packet.src().getSocket(), packet.nb_uc()));
        if(server.isRoomReady(computeId)){
            var process = new SharingProcessExecutor(
                    server.availableSocketsUc(computeId),
                    entity.info().end() - entity.info().start()
            );
            var socketRangeList = process.shareAndGet(entity.info().start());
            for(var socketRange: socketRangeList){
                var workAssignmentPacket = new WorkAssignmentPacket(
                        server.getAddress(),
                        socketRange.socketAddress(),
                        packet.requestID(),
                        socketRange.range()
                );
                server.transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }
}
