package fr.ramatellier.greed.server.compute;

import fr.ramatellier.greed.server.packet.frame.WorkAssignmentPacket;

public record ResponseTaskComputation(WorkAssignmentPacket packet, ComputationIdentifier id, long value, String response, byte code) {}
