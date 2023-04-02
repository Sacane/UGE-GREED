package fr.ramatellier.greed.server.util;

public final class Tuple<T, U> {
    public final T first;
    public final U second;

    public Tuple(T first, U second){
        this.first = first;
        this.second = second;
    }
    public T first(){
        return first;
    }
    public U second(){
        return second;
    }
    public static <T, U> Tuple<T, U> of(T first, U second){
        return new Tuple<>(first, second);
    }
}