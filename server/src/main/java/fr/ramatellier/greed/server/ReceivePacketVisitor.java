package fr.ramatellier.greed.server;


import fr.ramatellier.greed.server.compute.ComputationEntity;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.SharingProcessExecutor;
import fr.ramatellier.greed.server.compute.SocketUcIdentifier;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
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
        var addNodePacket = new AddNodePacket(new IDPacket(server.getAddress()), packet.daughter());
        server.broadcast(addNodePacket, packet.src().getSocket());
    }

    @Override
    public void visit(WorkRequestPacket packet) {
        if(server.getAddress().equals(packet.getIdDst().getSocket())) {
            System.out.println("RECEIVE A WORK REQUEST PACKET FOR ME");
            var deltaComputingPossibility = Server.MAXIMUM_COMPUTATION - server.currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server.tools().room().add(
                        new ComputationEntity(new ComputationIdentifier(packet.getRequestId(), packet.getIdSrc().getSocket()),
                                new ComputeInfo(packet.getChecker().url(), packet.getChecker().className(), packet.getRange().start(), packet.getRange().end()))
                );
                server.transfer(packet.getIdSrc().getSocket(), new WorkRequestResponsePacket(
                        packet.getIdSrc(),
                        packet.getIdDst(),
                        packet.getRequestId(),
                        deltaComputingPossibility
                ));
            }
        }
        else {
            System.out.println("RECEIVE A COMPUTATION FROM " + packet.getIdSrc().getSocket() + " FOR " + packet.getIdDst().getSocket());
            server.transfer(packet.getIdDst().getSocket(), packet);
        }
    }

    /**
     * In case we receive a workResponsePacket, we check if we are the destination of the packet.
     * If so, we just print the result of the computation.
     * @param packet the packet to visit
     */
    @Override
    public void visit(WorkAssignmentPacket packet) {
        var hasBeenTransfer = packet.onConditionTransfer(!packet.getIdDst().getSocket().equals(server.getAddress()), packet.getIdDst().getSocket(), server);
        if(hasBeenTransfer){
            System.out.println("RECEIVE A WORK ASSIGNMENT PACKET TO TRANSFER FOR " + packet.getIdDst().getSocket());
            return;
        }
        System.out.println("RECEIVE PACKET ASSIGNMENT");
        var idContext = new ComputationIdentifier(packet.getRequestId(), packet.getIdSrc().getSocket());
        var entityResponse = server.tools().room().findById(idContext);
        if(entityResponse.isEmpty()){
            System.out.println("THIS SERVER DOES NOT HANDLE THIS COMPUTATION");
            return;
        }
        var entity = entityResponse.get();
        var targetRange = packet.getRanges().get(0); //TODO REMOVE LIST
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
            System.out.println("Checker sent for " + i); //TODO REMOVE IT LATER
        }
    }
    private void sendResponseWithOPCode(WorkAssignmentPacket origin, long index,String result, byte opcode){
        server.transfer(origin.getIdSrc().getSocket(), new WorkResponsePacket(
                origin.getIdDst(),
                origin.getIdSrc(),
                origin.getRequestId(),
                new ResponsePacket(index, result, opcode)
        ));
    }
    @Override
    public void visit(WorkResponsePacket packet) {
        if(packet.onConditionTransfer(
                !server.getAddress().equals(packet.dst().getSocket()),
                packet.dst().getSocket(),
                server
        )){
            System.out.println("RECEIVE A WORK RESPONSE PACKET TO TRANSFER FOR " + packet.dst().getSocket());
            return;
        }
        System.out.println("RECEIVED A RESULT FROM " + packet.src().getSocket() + " FOR COMPUTATION " + packet.requestID());
        var responsePacket = packet.responsePacket();
        switch(packet.responsePacket().getResponseCode()){
            case 0x00 -> {
                System.out.println(responsePacket.getResponse().value());
            }
            case 0x01 -> {
                System.out.println("An exception has occur while computing the value : " + responsePacket.getValue());
            }
            case 0x02 -> {
                System.out.println("Time out while computing the value : " + responsePacket.getValue());
            }
            case 0x03 -> {
                System.out.println("Cannot get the checker");
            }
            default -> {
                logger.severe("UNKNOWN RESPONSE CODE");
            }
        }
    }

    @Override
    public void visit(LogoutRequestPacket packet) {
        System.out.println("RECEIVE LOGOUT");

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
        System.out.println("LOGOUT ACCEPTED");

        var daughtersContext = server.daughtersContext();

        for(var daughterContext: daughtersContext) {
            daughterContext.queuePacket(new PleaseReconnectPacket(server.getParentSocketAddress()));
        }
    }

    @Override
    public void visit(PleaseReconnectPacket packet) {
        System.out.println("PLEASE RECONNECT PACKET");
        System.out.println("TO " + packet.getId().getSocket());

        try {
            server.connectToNewParent(packet.getId());
        } catch (IOException e) {
            System.out.println("FAIL RECONNECT TO NEW PARENT");
        }
    }

    @Override
    public void visit(ReconnectPacket packet) {
        System.out.println("RECONNECT PACKET");
        System.out.println("FROM " + packet.getId().getSocket());

        server.receiveReconnect(packet.getId().getSocket());
        server.addRoot(packet.getId().getSocket(), packet.getId().getSocket(), context);

        for(var id: packet.getAncestors()) {
            server.addRoot(id.getSocket(), packet.getId().getSocket(), context);
        }

        if(server.allConnected()) {
            System.out.println("TOUT LE MONDE EST CONNECT");
            server.broadcast(new DisconnectedPacket(server.getAddress(), server.getAddressLogout()), server.getAddress());
            server.deleteAddress(server.getAddressLogout());
        }
    }

    @Override
    public void visit(DisconnectedPacket packet) {
        System.out.println("DISCONNECTED PACKET");

        if(server.getAddress().equals(packet.getId().getSocket())) {
            server.shutdown();
        }
        else {
            server.deleteAddress(packet.getId().getSocket());
            server.broadcast(new DisconnectedPacket(server.getAddress(), packet.getId().getSocket()), packet.getIdSrc().getSocket());
        }
    }

    @Override
    public void visit(WorkRequestResponsePacket packet) {
        var transfer = packet.onConditionTransfer(
                !server.getAddress().equals(packet.dst().getSocket()),
                packet.dst().getSocket(),
                server
        );
        if(transfer){
            System.out.println("RECEIVE A WORK RESPONSE PACKET TO TRANSFER FOR " + packet.dst().getSocket());
            return;
        }
        //Computation sender part
        if(packet.nb_uc() == 0){
            return;
        }
        var computeId = new ComputationIdentifier(packet.requestID(), server.getAddress());
        var store = server.tools().reminder();
        var room = server.tools().room();
        var entityResponse = room.findById(computeId);
        if(entityResponse.isEmpty()){
            logger.severe("No computation found for id " + computeId);
            return;
        }
        var entity = entityResponse.get();
        room.increment(computeId);

        store.storeSocketFor(
                computeId,
                new SocketUcIdentifier(packet.src().getSocket(), packet.nb_uc())
        );
        store.print(computeId);
        if(room.isReady(computeId)){
            var process = new SharingProcessExecutor(
                    store.availableSockets(computeId),
                    entity.info().end() - entity.info().start()
            );
            var socketRangeList = process.shareAndGet(entity.info().start());
            for(var socketRange: socketRangeList){
                System.out.println("DISTRIBUTING TO " + socketRange.socketAddress());
                var workAssignmentPacket = new WorkAssignmentPacket(
                        server.getAddress(),
                        socketRange.socketAddress(),
                        packet.requestID(),
                        List.of(new RangePacket(socketRange.range().start(), socketRange.range().end()))
                );
                server.transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }
}
