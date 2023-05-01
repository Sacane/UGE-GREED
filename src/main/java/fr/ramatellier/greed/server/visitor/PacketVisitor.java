package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.frame.model.*;

public interface PacketVisitor {
    void visit(ConnectFrame packet);
    void visit(ConnectOKFrame packet);
    void visit(ConnectKOFrame packet);
    void visit(AddNodeFrame packet);
    void visit(WorkRequestFrame packet);
    void visit(WorkAssignmentFrame packet);
    void visit(WorkResponseFrame packet);
    void visit(WorkRequestResponseFrame packet);
    void visit(LogoutRequestFrame packet);
    void visit(LogoutDeniedFrame packet);
    void visit(LogoutGrantedFrame packet);
    void visit(PleaseReconnectFrame packet);
    void visit(ReconnectFrame packet);
    void visit(DisconnectedFrame packet);

    default void visit(Frame packet) {
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
}
