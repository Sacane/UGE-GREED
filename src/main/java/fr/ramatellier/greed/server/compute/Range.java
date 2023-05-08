package fr.ramatellier.greed.server.compute;

public record Range(long start, long end) {

    public Range {
        if (start > end) {
            throw new IllegalArgumentException("Start must be lower than end");
        }
    }
    public long delta(boolean strict) {
        return end - start - (strict ? 1 : 0);
    }
    public static Range empty() {
        return new Range(0, 0);
    }

    public boolean isFull(){
        return start == end;
    }
}
