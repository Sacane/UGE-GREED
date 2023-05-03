package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.FrameKind;

public sealed interface LocalFrame extends Frame permits ConnectKOFrame, ConnectOKFrame, ConnectFrame, LogoutDeniedFrame, LogoutGrantedFrame, LogoutRequestFrame, PleaseReconnectFrame, ReconnectFrame {
    default FrameKind kind(){
        return FrameKind.LOCAL;
    }
}
