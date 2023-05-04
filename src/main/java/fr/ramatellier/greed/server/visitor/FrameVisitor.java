package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.compute.*;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.util.http.NonBlockingHTTPJarProvider;
import fr.uge.ugegreed.Checker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.LongStream;

public abstract class FrameVisitor {

    private final Logger logger = Logger.getLogger(FrameVisitor.class.getName());

    public abstract Server server();
    public abstract Context context();
    public void visit(Frame packet) {
        switch(packet) {
            case ConnectFrame p -> visit(p);
            case ConnectOKFrame p -> visit(p);
            case ConnectKOFrame p -> visit(p);
            case AddNodeFrame p -> visit(p);
            case WorkRequestFrame p -> visit(p);
            case WorkAssignmentFrame p -> visit(p);
            case WorkResponseFrame p -> visit(p);
            case WorkRequestResponseFrame p -> visit(p);
            case LogoutRequestFrame p -> visit(p);
            case LogoutDeniedFrame p -> visit(p);
            case LogoutGrantedFrame p -> visit(p);
            case PleaseReconnectFrame p -> visit(p);
            case ReconnectFrame p -> visit(p);
            case DisconnectedFrame p -> visit(p);
        }
    }

    protected void visit(ConnectFrame packet) {
        logger.info("Connection demand received from " + packet.idPacket().getSocket() + " " + packet.idPacket().getPort());
        if(server().isRunning()) {
            logger.info("Connection accepted");
            var list = new IDListComponent(server().registeredAddresses().stream().map(IDComponent::new).toList());
            System.out.println("List of neighbors: " + list);
            var response = new ConnectOKFrame(new IDComponent(server().getAddress()),
                    list);
            context().queuePacket(response);
            InetSocketAddress socket = packet.idPacket().getSocket();
            server().addRoot(socket, socket, context());
            var addNodePacket = new AddNodeFrame(new IDComponent(server().getAddress()), new IDComponent(socket));
            server().broadcast(addNodePacket, socket);
        }
        else {
            logger.info("Connection refused");
            context().queuePacket(new ConnectKOFrame());
        }
    }
    protected void visit(AddNodeFrame packet) {
        logger.info("AddNodePacket received from " + packet.src().getSocket());
        server().addRoot(packet.daughter().getSocket(), packet.src().getSocket(), context());
        logger.info("update root table and send broadcast to neighbours");
    }
    protected void visit(WorkRequestFrame packet) {
        if(server().isRunning()) {
            var deltaComputingPossibility = Server.MAXIMUM_COMPUTATION - server().currentOnWorkingComputationsValue();
            if(deltaComputingPossibility > 0) { //He is accepting the computation
                server().addRoom(new ComputationEntity(new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket()),
                        new ComputeInfo(packet.checker().url(), packet.checker().className(), packet.range().start(), packet.range().end())));
                server().transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
                        packet.src(),
                        packet.dst(),
                        packet.requestId(),
                        LongComponent.of(deltaComputingPossibility)
                ));
            }
        }
        else {
            server().transfer(packet.src().getSocket(), new WorkRequestResponseFrame(
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
    protected void visit(WorkAssignmentFrame packet) {
        System.out.println(packet.getClass() + " " + context().getClass().getName());
        System.out.println("Start computation...");
        var idContext = new ComputationIdentifier(packet.requestId().get(), packet.src().getSocket());
        server().updateRoom(idContext, packet.range().start(), packet.range().end());
        var entityResponse = server().findComputationById(idContext);
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
                    server().addTask(new TaskComputation(packet, checker, entity.id(), i));
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
        server().transfer(origin.src().getSocket(), new WorkResponseFrame(
                origin.dst(),
                origin.src(),
                origin.requestId(),
                new ResponseComponent(index, result, opcode)
        ));
    }
    protected void visit(WorkResponseFrame packet) {
        System.out.println(packet.getClass() + " " + context().getClass().getName());
        var responsePacket = packet.responsePacket();

        switch(responsePacket.getResponseCode()) {
            case 0x00 -> System.out.println(responsePacket.getResponse().value());
            case 0x01 -> System.out.println("An exception has occur while computing the value : " + responsePacket.getValue());
            case 0x02 -> System.out.println("Time out while computing the value : " + responsePacket.getValue());
            case 0x03 -> System.out.println("Cannot get the checker");
            default -> logger.severe("UNKNOWN RESPONSE CODE");
        }

        var id = new ComputationIdentifier(packet.requestID().get(), server().getAddress());
        try {
            server().treatComputationResult(id, packet.result());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "The file cannot be written : ", e);
        }
    }

    protected void visit(WorkRequestResponseFrame packet) {
        System.out.println(packet.getClass() + " " + context().getClass().getName());
        if(packet.nb_uc().get() == 0){
            return;
        }
        var computeId = new ComputationIdentifier(packet.requestID().get(), server().getAddress());
        var entity = server().retrieveWaitingComputation(computeId);
        if(entity == null){
            return;
        }
        server().incrementWaitingWorker(computeId);
        server().storeComputation(computeId, new SocketUcIdentifier(packet.src().getSocket(), packet.nb_uc().get()));
        if(server().isRoomReady(computeId)){
            var process = new SharingProcessExecutor(
                    server().availableSocketsUc(computeId),
                    entity.info().end() - entity.info().start()
            );
            var socketRangeList = process.shareAndGet(entity.info().start());
            for(var socketRange: socketRangeList){
                var workAssignmentPacket = new WorkAssignmentFrame(
                        new IDComponent(server().getAddress()),
                        new IDComponent(socketRange.socketAddress()),
                        packet.requestID(),
                        new RangeComponent(socketRange.range().start(), socketRange.range().end())
                );
                server().transfer(socketRange.socketAddress(), workAssignmentPacket);
            }
        }
    }
    protected void visit(LogoutRequestFrame packet){}
    protected void visit(LogoutDeniedFrame packet){}
    protected void visit(LogoutGrantedFrame packet){}
    protected void visit(PleaseReconnectFrame packet){}
    protected void visit(ReconnectFrame packet){}
    protected void visit(DisconnectedFrame packet){}
    protected void visit(ConnectOKFrame packet){}
    protected void visit(ConnectKOFrame packet){}


}
