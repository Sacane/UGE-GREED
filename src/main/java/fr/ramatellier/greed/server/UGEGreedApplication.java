package fr.ramatellier.greed.server;

import java.io.IOException;

public class UGEGreedApplication {
    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length != 1 && args.length != 3) {
            usage();
            return;
        }
        var port = Integer.parseInt(args[0]);
        if (args.length == 1) {
            Application.root(port).launch();
        } else {
            var ip = args[1];
            var remotePort = Integer.parseInt(args[2]);
            Application.child(port, ip, remotePort).launch();
        }
    }

    private static void usage() {
        System.out.println("Usage (ROOT MODE) : Server port");
        System.out.println("Usage (CONNECTED MODE) : Server port IP port");
    }
}
