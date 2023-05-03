package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.CheckerComponent;
import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.frame.component.primitive.LongComponent;

public record WorkRequestFrame(IDComponent src, IDComponent dst, LongComponent requestId, CheckerComponent checker, RangeComponent range, LongComponent max) implements TransferFrame {

}
