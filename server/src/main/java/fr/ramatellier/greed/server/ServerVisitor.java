package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.packet.ConnectKOPacket;
import fr.ramatellier.greed.server.packet.ConnectOKPacket;
import fr.ramatellier.greed.server.packet.ConnectPacket;
import fr.ramatellier.greed.server.packet.PacketVisitor;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ServerVisitor implements PacketVisitor {

    private final Server server;
    private final Context context;

    public ServerVisitor(Server server, Context context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    public Context getContext(){
        return context;
    }

    @Override
    public void visit(ConnectPacket packet) {
        var address = packet.getAddress();
        var port = packet.getPort();
        var socket = packet.getSocket();

        //send OKPacket
        if(server.isRunning()){
            var buffer = ByteBuffer.allocate(1024);
        }

    }

    @Override
    public void visit(ConnectOKPacket packet) {
    }

    @Override
    public void visit(ConnectKOPacket packet) {

    }

}
