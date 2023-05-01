package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.FrameKind;

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
