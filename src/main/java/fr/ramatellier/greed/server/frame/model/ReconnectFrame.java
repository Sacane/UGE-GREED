package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.IDListComponent;

public record ReconnectFrame(IDComponent id, IDListComponent ancestors) implements LocalFrame {

}
