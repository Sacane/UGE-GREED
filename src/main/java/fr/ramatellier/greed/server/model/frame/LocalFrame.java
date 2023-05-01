package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.util.FrameKind;

public sealed interface LocalFrame extends Frame permits ConnectKOFrame, ConnectOKFrame, ConnectFrame, LogoutDeniedFrame, LogoutGrantedFrame, LogoutRequestFrame, PleaseReconnectFrame, ReconnectFrame {
    default FrameKind kind(){
        return FrameKind.LOCAL;
    }
}
