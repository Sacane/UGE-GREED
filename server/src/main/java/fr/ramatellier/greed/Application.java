package fr.ramatellier.greed;

public class Application {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Thread.ofPlatform().start(() -> {
            System.out.println("Hello World from thread!");
        });
    }
}
