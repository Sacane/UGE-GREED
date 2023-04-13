package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.compute.ComputeInfo;
import fr.ramatellier.greed.server.ServerApplicationContext;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.compute.ComputationEntity;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.SharingProcessExecutor;
import fr.ramatellier.greed.server.compute.SocketUcIdentifier;
import fr.ramatellier.greed.server.packet.full.*;
import fr.ramatellier.greed.server.packet.sub.*;
import fr.uge.ugegreed.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    private final ServerApplicationContext context;
    private static final Logger logger = Logger.getLogger(ReceivePacketVisitor.class.getName());

    public ReceivePacketVisitor(Server server, ServerApplicationContext context) {
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
                server.addRoom(new ComputationEntity(new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket()),
                        new ComputeInfo(packet.checker().url(), packet.checker().className(), packet.range().start(), packet.range().end())));
                server.transfer(packet.src().getSocket(), new WorkRequestResponsePacket(
                        packet.src(),
                        packet.dst(),
                        packet.requestId(),
                        new LongPacketPart(deltaComputingPossibility)
                ));
            }
        }
        else {
            server.transfer(packet.src().getSocket(), new WorkRequestResponsePacket(
                    packet.src(),
                    packet.dst(),
                    packet.requestId(),
                    new LongPacketPart(0L)
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
        var idContext = new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket());
        server.updateRoom(idContext, packet.range().start(), packet.range().end());
        var entityResponse = server.findComputationById(idContext);
        if(entityResponse.isEmpty()) {
            return ;
        }
        var entity = entityResponse.get();
        var targetRange = packet.range();
        var result = Client.checkerFromHTTP(entity.info().url(), entity.info().className());
        if(result.isEmpty()) {
            logger.severe("CANNOT GET THE CHECKER");
            LongStream.range(targetRange.start(), targetRange.end()).forEach(i -> sendResponseWithOPCode(packet, i, "CANNOT GET THE CHECKER", (byte) 0x03));
            return;
        }
        var checker = result.get();

        Thread.ofPlatform().start(() -> {
            for(var i = targetRange.start(); i < targetRange.end(); i++) {
                try{
                    var checkerResult = checker.check(i);
                    sendResponseWithOPCode(packet, i, checkerResult, (byte) 0x00);
                } catch (InterruptedException e) {
                    logger.severe("INTERRUPTED EXCEPTION");
                    sendResponseWithOPCode(packet, i, null, (byte) 0x01);
                } catch (Exception e){
                    sendResponseWithOPCode(packet, i, null, (byte) 0x01);
                }

                server.incrementComputation(entity.id());
            }
            System.out.println("Computation finished");
            if(server.isShutdown() && !server.isComputing()) {
                server.sendLogout();
            }

            server.wakeup();
        });

        /*
        var lock = new ReentrantLock();
        var results = new HashMap<Long, String>();

        for(var i = targetRange.start(); i < targetRange.end(); i++) {
            var value = i;

            Thread.ofPlatform().start(() -> {
                var checkerResult = "";

                try{
                    checkerResult = checker.check(value);
                } catch (Exception e) {
                    // Ignore exception
                }

                lock.lock();
                try {
                    results.put(value, checkerResult);
                    server.incrementComputation(entity.id());
                    // sendResponseWithOPCode(packet, value, checkerResult, (byte) 0x00);
                } finally {
                    lock.unlock();
                }
            });
        }

        while(results.size() != targetRange.end() - targetRange.start()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        for(var entry: results.entrySet()) {
            sendResponseWithOPCode(packet, entry.getKey(), entry.getValue(), (byte) 0x00);
        }

        if(server.isShutdown() && !server.isComputing()) {
            server.sendLogout();
        }

        server.wakeup();
        */
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
        switch(packet.responsePacket().getResponseCode()){
            case 0x00 -> {
                System.out.println(responsePacket.getResponse().value());
                var id = new ComputationIdentifier(packet.requestID().get(), server.getAddress());
                try {
                    server.treatComputationResult(id, packet.result());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "The file cannot be written : ", e);
                }
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
            System.out.println("LOGOUT REQUEST");
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
