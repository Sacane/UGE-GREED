package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.ClientApplicationContext;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.compute.ComputationEntity;
import fr.ramatellier.greed.server.compute.ComputationIdentifier;
import fr.ramatellier.greed.server.compute.ComputeInfo;
import fr.ramatellier.greed.server.compute.TaskComputation;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.ResponseComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;
import fr.ramatellier.greed.server.frame.model.*;
import fr.ramatellier.greed.server.util.http.NonBlockingHTTPJarProvider;
import fr.uge.ugegreed.Checker;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.LongStream;

public class DaughterToParentVisitor implements FrameVisitor{
    private final ClientApplicationContext context;
    private final Server server;

    private static final Logger logger = Logger.getLogger(DaughterToParentVisitor.class.getName());

    /**
     * connectOK -> Client
     * AddNode -> Client
     * ConnectFrame -> Server
     * LogoutGranted -> Client
     * Disconnect -> Client
     * LogoutRequest -> Server
     * Reconnect -> Server
     * PleaseReconnect -> Client
     * WorkResponse -> Server
     * WorkRequestResponse -> Server
     * WorkRequest -> Client
     * WorkAssignment -> Client
     * LogoutDenied -> Client
     * ConnectKO -> Client
     * @param server
     * @param context
     */
    public DaughterToParentVisitor(Server server, ClientApplicationContext context){
        Objects.requireNonNull(context);
        Objects.requireNonNull(server);
        this.context = context;
        this.server = server;
    }

    @Override
    public void visit(ConnectKOFrame packet) {
        server.shutdown();
    }

    public void visit(ConnectOKFrame packet){
        var addressMother = packet.idMother();
        server.updateParentAddress(addressMother.getSocket());
        for(var neighbor: packet.neighbours().idPacketList()) {
            System.out.println("Add neighbor " + neighbor.getSocket() + " to root table");
            server.addRoot(neighbor.getSocket(), addressMother.getSocket(), context);
        }
        server.addRoot(addressMother.getSocket(), addressMother.getSocket(), context);
    }

    @Override
    public void visit(AddNodeFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        logger.info("AddNodePacket received from " + packet.src().getSocket());
        server.addRoot(packet.daughter().getSocket(), packet.src().getSocket(), context);
        logger.info("update root table and send broadcast to neighbours");
    }

    @Override
    public void visit(WorkRequestFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
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
        System.out.println(packet.getClass() + " " + context.getClass().getName());
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
    public void visit(LogoutDeniedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        System.out.println("LOGOUT DENIED");
    }
    @Override
    public void visit(LogoutGrantedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        System.out.println("LOGOUT GRANTED");
        var daughtersContext = server.daughtersContext();

        for(var daughterContext: daughtersContext) {
            daughterContext.queuePacket(new PleaseReconnectFrame(new IDComponent(server.getParentSocketAddress())));
        }
    }

    @Override
    public void visit(PleaseReconnectFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
        try {
            server.connectToNewParent(packet.id());
        } catch (IOException e) {
            // Ignore exception
        }
    }
    @Override
    public void visit(DisconnectedFrame packet) {
        System.out.println(packet.getClass() + " " + context.getClass().getName());
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

}
