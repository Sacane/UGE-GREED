package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;

public record AddNodeFrame(IDComponent src, IDComponent daughter) implements BroadcastFrame {
    @Override
    public BroadcastFrame withNewSource(IDComponent newSrc) {
        return new AddNodeFrame(newSrc, daughter);
    }
}
