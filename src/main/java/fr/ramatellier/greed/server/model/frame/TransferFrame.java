package fr.ramatellier.greed.server.model.frame;

import fr.ramatellier.greed.server.model.component.IDComponent;
import fr.ramatellier.greed.server.util.FrameKind;

public sealed interface TransferFrame extends Frame permits WorkAssignmentFrame, WorkRequestFrame, WorkRequestResponseFrame, WorkResponseFrame {
    IDComponent src();
    IDComponent dst();

    @Override
    default FrameKind kind(){
        return FrameKind.TRANSFER;
    }
}
