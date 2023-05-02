package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;

public record WorkAssignmentFrame(IDComponent src, IDComponent dst, long requestId, RangeComponent range) implements TransferFrame {

}
