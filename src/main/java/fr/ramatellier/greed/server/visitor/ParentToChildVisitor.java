package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.Context;
import fr.ramatellier.greed.server.Server;
import fr.ramatellier.greed.server.ServerApplicationContext;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.model.*;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Visitor for packets received by the server.
 * The context linked to this visitor is the context allowing to communicate with the sender.
 */
public class ParentToChildVisitor extends FrameVisitor {
    private final Server server;
    private final ServerApplicationContext context;
    private static final Logger logger = Logger.getLogger(ParentToChildVisitor.class.getName());

    public ParentToChildVisitor(Server server, ServerApplicationContext context) {
        this.server = Objects.requireNonNull(server);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public Server server() {
        return server;
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
