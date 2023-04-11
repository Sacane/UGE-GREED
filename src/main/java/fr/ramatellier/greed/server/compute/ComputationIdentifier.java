package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;

public record ComputationIdentifier(long id, InetSocketAddress src) {
    public String outputTitle(){
        return "result_" + id + ".txt";
    }
}
