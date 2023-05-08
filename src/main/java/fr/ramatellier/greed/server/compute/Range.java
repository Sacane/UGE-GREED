package fr.ramatellier.greed.server.compute;

public record Range(long start, long end) {
    public long delta(boolean strict) {

        return end - start - (strict ? 1 : 0);
    }
    public static Range empty() {
        return new Range(0, 0);
    }
    public boolean isEmpty(){
        return start == 0 && end == 0;
    }
    public boolean isFull(){
        return start == end;
    }
}
