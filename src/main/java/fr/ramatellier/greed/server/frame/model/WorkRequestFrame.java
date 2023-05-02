package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.CheckerComponent;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;

public record WorkRequestFrame(IDComponent src, IDComponent dst, long requestId, CheckerComponent checker, RangeComponent range, long max) implements TransferFrame {

}
