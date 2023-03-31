package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.compute.ComputationRoomHandler;
import fr.ramatellier.greed.server.compute.SocketCandidateReminder;

public record ServerProcessTool(SocketCandidateReminder reminder, ComputationRoomHandler room) {
    public static ServerProcessTool create() {
        var reminder = new SocketCandidateReminder();
        var room = new ComputationRoomHandler();
        return new ServerProcessTool(reminder, room);
    }
}
