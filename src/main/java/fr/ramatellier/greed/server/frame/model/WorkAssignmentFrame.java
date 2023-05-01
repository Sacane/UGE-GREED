package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.component.RangeComponent;
import fr.ramatellier.greed.server.util.OpCodes;

public record WorkAssignmentFrame(IDComponent src, IDComponent dst, long requestId, RangeComponent range) implements TransferFrame {

    @Override
    public OpCodes opCode() {
        return OpCodes.WORK_ASSIGNMENT;
    }

}
