package RS2.model.player;

public interface Packet {

	public void handlePacket(Client client, int packetType, int packetSize);

}