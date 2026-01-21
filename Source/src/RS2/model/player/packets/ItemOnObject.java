package RS2.model.player.packets;

import RS2.admin.AdminPanel;

/**
 * @author Ryan / Lmctruck30
 */

import RS2.model.item.UseItem;
import RS2.model.player.Client;
import RS2.model.player.PacketType;

@SuppressWarnings("all")
public class ItemOnObject implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		/*
		 * a = ?
		 * b = ?
		 */
		
		int a = c.getInStream().readUnsignedWord();
		int objectId = c.getInStream().readSignedWordBigEndian();
		int objectY = c.getInStream().readSignedWordBigEndianA();
		int b = c.getInStream().readUnsignedWord();
		int objectX = c.getInStream().readSignedWordBigEndianA();
		int itemId = c.getInStream().readUnsignedWord();
		UseItem.ItemonObject(c, objectId, objectX, objectY, itemId);
		AdminPanel.pushAgentEvent("use_item_on_object", "{\"player\":\"" + c.playerName + "\",\"object_id\":" + objectId + ",\"object_x\":" + objectX + ",\"object_y\":" + objectY + ",\"item_id\":" + itemId + "}");
	}

}
