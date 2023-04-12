package fr.ramatellier.greed.server.compute;

import java.net.InetSocketAddress;

/**
 * Represent a computation identifier, defined by a long ID and the source as address.
 * @param id the id of the computation
 * @param src the source of the computation
 */
public record ComputationIdentifier(long id, InetSocketAddress src) {
    public String outputTitle(){
        return "result_" + id + ".txt";
    }
}
