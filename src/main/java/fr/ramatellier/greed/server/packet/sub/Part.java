package fr.ramatellier.greed.server.packet.sub;

public sealed interface Part permits BytePacketPart, CheckerPacket, DestinationPacket, IDPacket, IDPacketList, IntPacketPart, IpAddressPacket, LongPacketPart, RangePacket, ResponsePacket, StringPacket {

}
