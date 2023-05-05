package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.context.ClientApplicationContext;
import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.frame.model.*;

import java.util.Objects;
import java.util.logging.Logger;

public class ChildReceiveVisitor extends ReceiveFrameVisitor {
    private final ClientApplicationContext context;

    private static final Logger logger = Logger.getLogger(ChildReceiveVisitor.class.getName());

    /**
     * @param context The context
     */
    public ChildReceiveVisitor(ClientApplicationContext context){
        Objects.requireNonNull(context);
        this.context = context;
    }

    @Override
    public void visit(ConnectKOFrame packet) {
        context.shutdownServer();
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public void visit(ConnectOKFrame packet){
        System.out.println("ConnectOKFrame");
        context.associate(packet.idMother(), packet.neighbours());
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
        context.reconnectDaughters();
    }

    @Override
    public void visit(PleaseReconnectFrame packet) {
        context.reconnect(packet.id());
    }
    @Override
    public void visit(DisconnectedFrame packet) {
        context.handleLogout(packet.id().getSocket());
    }
}
