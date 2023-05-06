package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.context.Context;
import fr.ramatellier.greed.server.frame.model.*;

import java.util.logging.Logger;

public abstract class ReceiveFrameVisitor {

    private final Logger logger = Logger.getLogger(ReceiveFrameVisitor.class.getName());

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
        context().handleConnection(packet.idPacket().getSocket());
    }
    protected void visit(AddNodeFrame packet) {
        logger.info("AddNodePacket received from " + packet.src().getSocket());
        context().updateRoot(packet.daughter().getSocket(), packet.src().getSocket(), context());
        logger.info("update root table and send broadcast to neighbours");
    }
    protected void visit(WorkRequestFrame packet) {
        context().processWorking(packet);
    }

    /**
     * In case we receive a workResponsePacket, we check if we are the destination of the packet.
     * If so, we just print the result of the computation.
     * @param packet the packet to visit
     */
    protected void visit(WorkAssignmentFrame packet) {
        context().compute(packet);
    }

    protected void visit(WorkResponseFrame packet) {
        context().handleResponse(packet.responsePacket(), packet.requestID().get(), packet.result());
    }

    protected void visit(WorkRequestResponseFrame packet) {
        context().handleRequestResponse(packet.nb_uc().get(), packet.requestID(), packet.src().getSocket());
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
