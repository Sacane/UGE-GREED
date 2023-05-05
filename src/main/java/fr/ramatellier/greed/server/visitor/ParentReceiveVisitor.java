package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.context.ServerApplicationContext;
import fr.ramatellier.greed.server.frame.model.*;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ParentReceiveVisitor extends ReceiveFrameVisitor {
    private final ServerApplicationContext context;
    private static final Logger logger = Logger.getLogger(ParentReceiveVisitor.class.getName());

    public ParentReceiveVisitor(ServerApplicationContext context) {
        this.context = Objects.requireNonNull(context);
    }
    @Override
    public Context context() {
        return context;
    }
    @Override
    public void visit(LogoutRequestFrame packet) {
        context.confirmLogout(packet.id(), packet.daughters());
    }
    @Override
    public void visit(ReconnectFrame packet) {
        context.reconnectServer(packet.id().getSocket(), packet.ancestors());
    }
}
