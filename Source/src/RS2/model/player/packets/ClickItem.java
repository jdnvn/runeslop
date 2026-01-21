package RS2.model.player.packets;

import RS2.admin.AdminPanel;
import RS2.model.player.Client;
import RS2.model.player.PacketType;

/**
 * Clicking an item, bury bone, eat food etc
 **/
@SuppressWarnings("all")
public class ClickItem implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int junk = c.getInStream().readSignedWordBigEndianA();
		int itemSlot = c.getInStream().readUnsignedWordA();
		int itemId = c.getInStream().readUnsignedWordBigEndian();
		if (itemId != c.playerItems[itemSlot] - 1) {
			return;
		}
		AdminPanel.pushAgentEvent("item_click", "{\"player\":\"" + c.playerName + "\",\"item_id\":" + itemId + ",\"item_slot\":" + itemSlot + "}");
	}
}
