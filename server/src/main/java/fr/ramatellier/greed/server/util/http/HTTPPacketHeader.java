package fr.ramatellier.greed.server.util.http;

public record HTTPPacketHeader(int code, int contentLength, String response, String version) {
}
