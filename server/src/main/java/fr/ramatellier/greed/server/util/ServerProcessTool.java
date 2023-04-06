package fr.ramatellier.greed.server.util;

import fr.ramatellier.greed.server.compute.ComputationRoomHandler;
import fr.ramatellier.greed.server.compute.SocketCandidate;


public record ServerProcessTool(SocketCandidate reminder, ComputationRoomHandler room) {
    public static ServerProcessTool create() {
        var reminder = new SocketCandidate();
        var room = new ComputationRoomHandler();
        return new ServerProcessTool(reminder, room);
    }
}
