package fr.ramatellier.greed.server.packet.sub;

public sealed interface Part permits CheckerPacket, DestinationPacket, IDPacket, IDPacketList, IpAddressPacket, RangePacket, ResponsePacket, StringPacket {

}
