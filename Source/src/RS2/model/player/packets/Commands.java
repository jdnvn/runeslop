package RS2.model.player.packets;

import RS2.Settings;
import RS2.GameEngine;
import RS2.model.player.Client;
import RS2.model.player.PacketType;
import RS2.util.Misc;
import RS2.model.npc.NPCHandler;
import RS2.model.npc.NPC;
import RS2.db.DatabaseManager;
import RS2.admin.CommandHandler;

/**
 * Commands
 **/
public class Commands implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		String playerCommand = c.getInStream().readString();
		Misc.println(c.playerName + " playerCommand: " + playerCommand);
		if (Settings.SERVER_DEBUG)
			if (playerCommand.startsWith("/") && playerCommand.length() > 1) {
				if (c.clanId >= 0) {
					System.out.println(playerCommand);
					playerCommand = playerCommand.substring(1);
					GameEngine.clanChat.playerMessageToClan(c.playerId,
							playerCommand, c.clanId);
				} else {
					if (c.clanId != -1)
						c.clanId = -1;
					c.sendMessage("You are not in a clan.");
				}
				return;
			}

		// only admins can execute commands for now
		if (c.playerRights < 2) return;
		if (playerCommand.length() > 1 && playerCommand.startsWith("/")) {
			String commandString = playerCommand.substring(1);
			String result = CommandHandler.executeCommand(commandString, c);
			c.sendMessage(result);
		}
	}
}
