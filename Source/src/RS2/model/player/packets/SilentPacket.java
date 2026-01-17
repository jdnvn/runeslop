package RS2.model.player.packets;

import RS2.model.player.Client;
import RS2.model.player.PacketType;

/**
 * Slient Packet
 **/
public class SilentPacket implements PacketType {
	
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
			
	}	
}
