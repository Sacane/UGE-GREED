package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.ConnectKOPacket;
import fr.ramatellier.greed.server.packet.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.ConnectPacket;
import fr.ramatellier.greed.server.packet.PacketVisitor;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerVisitor implements PacketVisitor {

    private final Server server;
    private final Context context;
    private static final Logger logger = Logger.getLogger(ServerVisitor.class.getName());

    public ServerVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    public Context getContext(){
        return context;
    }

    @Override
    public void visit(ConnectPacket packet) {

        //send OKPacket
        if(server.isRunning()){
            logger.info("Connection demand received from " + packet.getAddress() + " " + packet.getPort());

            var response = new ConnectOKPacket(server.getAddress(), server.neighbours());
            context.queuePacket(response);

            var socket = packet.getSocket();
            server.addRoot(socket, socket);
        }

    }

    @Override
    public void visit(ConnectOKPacket packet) {
        logger.info("Connection accepted from " + packet.getAddress() + " on port " + packet.getPort());

        var addressMother = packet.getMotherAddress();
        for(var neighbor: packet.neighbours()) {
            server.addRoot(neighbor, addressMother);
        }
        server.addRoot(addressMother, addressMother);
    }

    @Override
    public void visit(ConnectKOPacket packet) {
        System.out.println("ConnectKOPacket");
    }

}
