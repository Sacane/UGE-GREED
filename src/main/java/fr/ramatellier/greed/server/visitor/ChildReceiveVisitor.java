package fr.ramatellier.greed.server.visitor;

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
    protected void visit(ConnectKOFrame packet) {
        context.shutdownServer();
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    protected void visit(ConnectOKFrame packet){
        context.link(packet.idMother(), packet.neighbours());
    }


    @Override
    protected void visit(LogoutDeniedFrame packet) {
        logger.warning("LOGOUT DENIED");
    }
    @Override
    protected void visit(LogoutGrantedFrame packet) {
       logger.info("LOGOUT GRANTED");
        context.reconnectDaughters();
    }

    @Override
    protected void visit(PleaseReconnectFrame packet) {
        context.reconnect(packet.id());
    }
    @Override
    protected void visit(DisconnectedFrame packet) {
        context.handleLogout(packet.id().getSocket());
    }
}
