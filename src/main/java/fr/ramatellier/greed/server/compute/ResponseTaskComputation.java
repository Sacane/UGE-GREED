package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.frame.model.WorkAssignmentFrame;

public record ResponseTaskComputation(WorkAssignmentFrame packet, ComputationIdentifier id, long value, String response, byte code) {}
