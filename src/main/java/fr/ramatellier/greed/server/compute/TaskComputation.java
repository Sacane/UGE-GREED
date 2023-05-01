package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.packet.frame.WorkAssignmentPacket;
import fr.uge.ugegreed.Checker;

public record TaskComputation(WorkAssignmentPacket packet, Checker checker, ComputationIdentifier id, long value) {}
