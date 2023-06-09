package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.frame.model.WorkAssignmentFrame;
import fr.uge.ugegreed.Checker;

public record TaskComputation(WorkAssignmentFrame packet, Checker checker, ComputationIdentifier id, long value) {}
