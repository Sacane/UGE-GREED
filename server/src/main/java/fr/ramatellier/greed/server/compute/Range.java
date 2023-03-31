package fr.ramatellier.greed.server.compute;

public record Range(long start, long end) {
    public long delta(boolean strict) {

        return end - start - (strict ? 1 : 0);
    }
}
