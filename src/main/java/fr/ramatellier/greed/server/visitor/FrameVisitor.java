package fr.ramatellier.greed.server.visitor;

import fr.ramatellier.greed.server.frame.model.*;

public interface FrameVisitor {
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

    default void visit(ConnectFrame packet){}
    default void visit(ConnectOKFrame packet){}
    default void visit(ConnectKOFrame packet){}
    default void visit(AddNodeFrame packet){}
    default void visit(WorkRequestFrame packet){}
    default void visit(WorkAssignmentFrame packet){}
    default void visit(WorkResponseFrame packet){}
    default void visit(WorkRequestResponseFrame packet){}
    default void visit(LogoutRequestFrame packet){}
    default void visit(LogoutDeniedFrame packet){}
    default void visit(LogoutGrantedFrame packet){}
    default void visit(PleaseReconnectFrame packet){}
    default void visit(ReconnectFrame packet){}
    default void visit(DisconnectedFrame packet){}


}
