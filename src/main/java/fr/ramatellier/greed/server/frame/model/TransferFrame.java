package fr.ramatellier.greed.server.frame.model;

import fr.ramatellier.greed.server.frame.component.IDComponent;
import fr.ramatellier.greed.server.frame.FrameKind;

public sealed interface TransferFrame extends Frame permits WorkAssignmentFrame, WorkRequestFrame, WorkRequestResponseFrame, WorkResponseFrame {
    IDComponent src();
    IDComponent dst();

    @Override
    default FrameKind kind(){
        return FrameKind.TRANSFER;
    }
}
