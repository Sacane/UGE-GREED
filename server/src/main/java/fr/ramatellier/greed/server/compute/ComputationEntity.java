package fr.ramatellier.greed.server.compute;

public record ComputationEntity(ComputationIdentifier id, Range range, String className, String hostTarget, String url, long uc_max) {

}