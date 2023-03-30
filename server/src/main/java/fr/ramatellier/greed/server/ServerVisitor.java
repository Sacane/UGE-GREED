package fr.ramatellier.greed.server;


import fr.ramatellier.greed.server.packet.*;

import java.io.IOException;
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
            System.out.println("RECEIVE A COMPUTATION FOR ME FROM " + packet.getIdSrc().getSocket());
            System.out.println("Destination : " + packet.getIdDst().getSocket() + " Source : " + packet.getIdSrc().getSocket());
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
        System.out.println("WORK ASSIGNMENT");
    }

    @Override
    public void visit(WorkResponsePacket packet) {
        if(packet.onConditionTransfer(
                server.getAddress().equals(packet.dst().getSocket()),
                packet.dst().getSocket(),
                server
        )){
            return;
        }
//        var responsePacket = packet.responsePacket();
//        switch(packet.responsePacket().getResponseCode()){
//            case 0x00 -> {
//                System.out.println("WE JUST RECEIVED GOOD RESPONSE FROM " + packet.src());
//            }
//            default -> {
//                System.out.println("WE JUST RECEIVED BAD RESPONSE FROM " + packet.src());
//            }
//        }
    }

    @Override
    public void visit(LogoutRequestPacket packet) {
        System.out.println("RECEIVE LOGOUT");

        if(server.isRunning()) {
            System.out.println(packet.getDaughters().size());
            for(var daughter: packet.getDaughters()) {
                System.out.println(daughter.getSocket());
            }

            context.queuePacket(new LogoutGrantedPacket());
        }
    }

    @Override
    public void visit(LogoutDeniedPacket packet) {
        System.out.println("LOGOUT DENIED");
    }

    @Override
    public void visit(LogoutGrantedPacket packet) {
        System.out.println("LOGOUT ACCEPTED");

        var daughters = server.daughtersContext();

        for(var daughter: daughters) {
            daughter.queuePacket(new PleaseReconnectPacket(server.getParentSocketAddress()));
        }
    }

    @Override
    public void visit(PleaseReconnectPacket packet) {
        System.out.println("PLEASE RECONNECT PACKET");

        System.out.println(packet.getId().getSocket() + " " + packet.getId().getPort());
        try {
            server.connectToNewParent(packet.getId().getHostname(), packet.getId().getPort());
        } catch (IOException e) {
        }
    }

    @Override
    public void visit(ReconnectPacket packet) {
        System.out.println("RECONNECT PACKET");
    }

    @Override
    public void visit(WorkRequestResponsePacket packet) {
        System.out.println("WORK REQUEST REPONSE PACKET RECEIVED");
        System.out.println(packet);
        var transfer = packet.onConditionTransfer(
                !server.getAddress().equals(packet.dst().getSocket()),
                packet.dst().getSocket(),
                server
        );
        if(transfer){
            return;
        }
        if(packet.nb_uc() == 0){
            return;
        }
    }

//    private void compute(WorkRequestPacket packet) {
//        var entity = packet.toComputationEntity();
////        computeWorkHandler.processComputation(packet.toComputationEntity());
//        var responseChecker = Client.checkerFromHTTP(entity.url(), entity.className());
//        if(responseChecker.isEmpty()){
//            logger.severe("INVALID response url or class name");
//            handleBadWorkingResponse(-1L, (byte)0x03, packet);
//            return;
//        }
//        var checker = responseChecker.get();
//        var range = entity.range();
//        for(long i = range.start(); i < range.end() + 1; i++) {
//            try {
//                var checkResponse = checker.check(i);
//                buildAndSendResponsePacket(i, (byte)0x00, checkResponse, packet);
//            } catch (InterruptedException e) {
//                logger.info("Interrupted while computing " + i);
//                return;
//            } catch (Exception e) {
//                handleBadWorkingResponse(i, (byte) 0x01, packet);
//            }
//        }
//    }

    private void buildAndSendResponsePacket(long l, byte responseCode, String response, WorkRequestPacket origin) {
        var responsePacket = new ResponsePacket(l, response, responseCode);
        var workResponsePacket = new WorkResponsePacket(origin.getIdSrc(), origin.getIdDst(), origin.getRequestId(), responsePacket);
        server.transfer(origin.getIdSrc().getSocket(), workResponsePacket);
    }

    private void handleBadWorkingResponse(long i, byte responseCode, WorkRequestPacket origin) {
        buildAndSendResponsePacket(i, responseCode, null, origin);
    }
}
