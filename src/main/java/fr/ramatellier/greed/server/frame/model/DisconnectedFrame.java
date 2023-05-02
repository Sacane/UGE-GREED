package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;

public record DisconnectedFrame(IDComponent src, IDComponent id) implements BroadcastFrame {


    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new DisconnectedFrame(newSrc, id);
    }


}
