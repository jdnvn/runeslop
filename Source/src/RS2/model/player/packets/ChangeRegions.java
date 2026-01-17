package RS2.model.player.packets;

import RS2.GameEngine;
import RS2.model.player.Client;
import RS2.model.player.PacketType;

/**
 * Change Regions
 */
public class ChangeRegions implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		//Server.objectHandler.updateObjects(c);
		GameEngine.itemHandler.reloadItems(c);
		GameEngine.objectManager.loadObjects(c);
		c.saveFile = true;
		if(c.skullTimer > 0) {
			c.isSkulled = true;	
			c.headIconPk = 0;
			c.getPA().requestUpdates();
		}
	}
}