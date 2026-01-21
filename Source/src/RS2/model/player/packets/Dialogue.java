package RS2.model.player.packets;

import RS2.model.player.Client;
import RS2.model.player.PacketType;
import RS2.admin.AdminPanel;

/**
 * Dialogue
 **/
public class Dialogue implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		// Push to agent via SSE
		AdminPanel.pushAgentEvent("dialogue_continue", 
			"{\"player\":\"" + c.playerName + "\",\"dialogueId\":" + c.nextChat + "}");
		
		if(c.nextChat > 0) {
			// If there's a hardcoded next dialogue, use it
			c.getDH().sendDialogues(c.nextChat, c.talkingNpc);
		} else {
			// No hardcoded follow-up - close the interface and let the agent handle it
			c.getPA().closeAllWindows();
		}
	}
}
