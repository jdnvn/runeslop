package RS2.model.player.packets;

import RS2.model.player.Client;
import RS2.model.player.PacketType;
import RS2.admin.AdminPanel;

/**
 * Chat
 **/
public class Chat implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.setChatTextEffects(c.getInStream().readUnsignedByteS());
		c.setChatTextColor(c.getInStream().readUnsignedByteS());
        c.setChatTextSize((byte)(c.packetSize - 2));
        c.inStream.readBytes_reverseA(c.getChatText(), c.getChatTextSize(), 0);
		c.setChatTextUpdateRequired(true);  // Flag to broadcast to nearby players

		String chatText = RS2.util.Misc.textUnpack(c.getChatText(), c.getChatTextSize());
		
		// Push to agent via SSE
		AdminPanel.pushAgentEvent("player_chat", 
			"{\"player\":\"" + c.playerName + "\",\"message\":\"" + chatText + "\"}");
	}	
}
