package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;

public record WorkAssignmentFrame(IDComponent src, IDComponent dst, LongComponent requestId, RangeComponent range) implements TransferFrame {

}
