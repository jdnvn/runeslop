package RS2.model.player.packets;

import RS2.admin.AdminPanel;
import RS2.model.item.UseItem;
import RS2.model.npc.NPCHandler;
import RS2.model.player.Client;
import RS2.model.player.PacketType;


public class ItemOnNpc implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int itemId = c.getInStream().readSignedWordA();
		int i = c.getInStream().readSignedWordA();
		int slot = c.getInStream().readSignedWordBigEndian();
		int npcId = NPCHandler.npcs[i].npcType;
		UseItem.ItemonNpc(c, itemId, npcId, slot);
		AdminPanel.pushAgentEvent("use_item_on_npc", "{\"player\":\"" + c.playerName + "\",\"item_id\":" + itemId + ",\"npc_id\":" + npcId + ",\"slot\":" + slot + "}");
	}
}
