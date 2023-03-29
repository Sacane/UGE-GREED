package fr.ramatellier.greed.server.compute;

public record Range(long start, long end) {
    public long delta() {
        return end - start;
    }
}
