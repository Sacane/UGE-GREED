package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.FrameKind;

public sealed interface BroadcastFrame extends Frame permits AddNodeFrame, DisconnectedFrame {
    @Override
    default FrameKind kind(){
        return FrameKind.BROADCAST;
    }

    /**
     * @return the source of the frame.
     */
    IDComponent src();

    /**
     * Create a new frame with the same content but a new source.
     * @param newSrc the new source.
     * @return a new frame with the same content but a new source.
     */
    BroadcastFrame withNewSource(IDComponent newSrc);
}
