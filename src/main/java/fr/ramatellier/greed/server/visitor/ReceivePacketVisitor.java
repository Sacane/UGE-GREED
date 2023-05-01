package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.IDPacket;
import fr.ramatellier.greed.server.packet.sub.IDPacketList;
import fr.ramatellier.greed.server.packet.sub.RangePacket;
import fr.ramatellier.greed.server.packet.sub.ResponsePacket;
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
        logger.info("Connection demand received from " + packet.idPacket().getSocket() + " " + packet.idPacket().getPort());
        if(server.isRunning()) {
            logger.info("Connection accepted");
            var list = new IDPacketList(server.registeredAddresses().stream().map(IDPacket::new).toList());
            System.out.println("List of neighbors: " + list);
            var response = new ConnectOKPacket(new IDPacket(server.getAddress()),
                    list);
            context.queuePacket(response);
            InetSocketAddress socket = packet.idPacket().getSocket();
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
        if(server.isRunning()) {
            var deltaComputingPossibility = Server.MAXIMUM_COMPUTATION - server.currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server.addRoom(new ComputationEntity(new ComputationIdentifier(packet.requestId(), packet.src().getSocket()),
                        new ComputeInfo(packet.checker().url(), packet.checker().className(), packet.range().start(), packet.range().end())));
                server.transfer(packet.src().getSocket(), new WorkRequestResponsePacket(
                        packet.src(),
                        packet.dst(),
                        packet.requestId(),
                        deltaComputingPossibility
                ));
            }
        }
        else {
            server.transfer(packet.src().getSocket(), new WorkRequestResponsePacket(
                    packet.src(),
                    packet.dst(),
                    packet.requestId(),
                    0L
            ));
        }
    }

    /**
     * In case we receive a workResponsePacket, we check if we are the destination of the packet.
     * If so, we just print the result of the computation.
     * @param packet the packet to visit
     */
    @Override
    public void visit(WorkAssignmentPacket packet) {
        System.out.println("Start computation...");
        var idContext = new ComputationIdentifier(packet.requestId(), packet.src().getSocket());
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
                var path = Path.of(httpClient.getFilePath());
                System.out.println(path);
                var checkerResult = Client.checkerFromDisk(path, entity.info().className());
                Checker checker;
                if(checkerResult.isEmpty()) {
                    logger.severe("CANNOT GET THE CHECKER");
                    checker = null;
                }
                else {
                    checker = checkerResult.get();
                }
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

    private void sendResponseWithOPCode(WorkAssignmentPacket origin, long index, String result, byte opcode) {
        server.transfer(origin.src().getSocket(), new WorkResponsePacket(
                origin.dst(),
                origin.src(),
                origin.requestId(),
                new ResponsePacket(index, result, opcode)
        ));
    }

    @Override
    public void visit(WorkResponsePacket packet) {
        var responsePacket = packet.responsePacket();

        switch(responsePacket.getResponseCode()) {
            case 0x00 -> System.out.println(responsePacket.getResponse().value());
            case 0x01 -> System.out.println("An exception has occur while computing the value : " + responsePacket.getValue());
            case 0x02 -> System.out.println("Time out while computing the value : " + responsePacket.getValue());
            case 0x03 -> System.out.println("Cannot get the checker");
            default -> logger.severe("UNKNOWN RESPONSE CODE");
        }

        var id = new ComputationIdentifier(packet.requestID(), server.getAddress());
        try {
            server.treatComputationResult(id, packet.result());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "The file cannot be written : ", e);
        }
    }

    @Override
    public void visit(LogoutRequestPacket packet) {
        if(server.isRunning()) {
            context.queuePacket(new LogoutGrantedPacket());
            if(packet.daughters().sizeList() == 0) {
                server.broadcast(new DisconnectedPacket(new IDPacket(server.getAddress()), packet.id()), server.getAddress());
                server.deleteAddress(packet.id().getSocket());
            }
            else {
                server.newLogoutRequest(packet.id().getSocket(), packet.daughters().idPacketList().stream().map(IDPacket::getSocket).toList());
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
        System.out.println("LOGOUT GRANTED");
        var daughtersContext = server.daughtersContext();

        for(var daughterContext: daughtersContext) {
            daughterContext.queuePacket(new PleaseReconnectPacket(new IDPacket(server.getParentSocketAddress())));
        }
    }

    @Override
    public void visit(PleaseReconnectPacket packet) {
        try {
            server.connectToNewParent(packet.id());
        } catch (IOException e) {
            // Ignore exception
        }
    }

    @Override
    public void visit(ReconnectPacket packet) {
        server.receiveReconnect(packet.id().getSocket());
        server.addRoot(packet.id().getSocket(), packet.id().getSocket(), context);

        for(var id: packet.ancestors().idPacketList()) {
            server.addRoot(id.getSocket(), packet.id().getSocket(), context);
        }

        if(server.allConnected()) {
            server.broadcast(new DisconnectedPacket(new IDPacket(server.getAddress()), new IDPacket(server.getAddressLogout())), server.getAddress());
            server.deleteAddress(server.getAddressLogout());

            if(server.isShutdown()) {
                server.sendLogout();
            }
        }
    }

    @Override
    public void visit(DisconnectedPacket packet) {
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
                        new IDPacket(server.getAddress()),
                        new IDPacket(socketRange.socketAddress()),
                        packet.requestID(),
                        new RangePacket(socketRange.range().start(), socketRange.range().end())
                );
                server.transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }
}
