package fr.ramatellier.greed.server;

import java.io.IOException;

public class UGEGreedApplication {
    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1 && args.length != 3) {
            usage();
            return;
        }
        if (args.length == 1) {
            Application.root(Integer.parseInt(args[0])).launch();
        } else {
            Application.child(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2])).launch();
        }
    }

    private static void usage() {
        System.out.println("Usage (ROOT MODE) : Server port");
        System.out.println("Usage (CONNECTED MODE) : Server port IP port");
    }
}
