package RS2.model.player.packets;

import RS2.model.player.Client;
import RS2.model.player.PacketType;

public class ItemClick2 implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int itemId = c.getInStream().readSignedWordA();
		if (!c.getItems().playerHasItem(itemId,1))
			return;
		switch (itemId) {
		}
	}
}