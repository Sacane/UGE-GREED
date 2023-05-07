package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.context.ServerApplicationContext;
import fr.ramatellier.greed.server.frame.model.LogoutRequestFrame;
import fr.ramatellier.greed.server.frame.model.ReconnectFrame;

import java.util.Objects;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ParentReceiveVisitor extends ReceiveFrameVisitor {
    private final ServerApplicationContext context;
    public ParentReceiveVisitor(ServerApplicationContext context) {
        this.context = Objects.requireNonNull(context);
    }
    @Override
    public Context context() {
        return context;
    }
    @Override
    protected void visit(LogoutRequestFrame packet) {
        context.confirmLogout(packet.id(), packet.daughters());
    }
    @Override
    protected void visit(ReconnectFrame packet) {
        context.reconnectServer(packet.id().getSocket(), packet.ancestors());
    }
}
