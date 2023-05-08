package fr.ramatellier.greed.server.util;

public final class Strings {
    private Strings() {}

    public static String inGreen(String text) {
        return "\u001B[32m" + text + "\u001B[0m";
    }
    public static String inRed(String text) {
        return "\u001B[31m" + text + "\u001B[0m";
    }

    public static void printInGreed(String text){
        System.out.println(inGreen(text));
    }
    public static void printInRed(String text){
        System.out.println(inRed(text));
    }
}
