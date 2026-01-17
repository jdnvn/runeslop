package RS2.model.player.packets;

import RS2.Settings;
import RS2.GameEngine;
import RS2.model.player.Client;
import RS2.model.player.PacketType;
import RS2.util.Misc;

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
			String[] args = commandString.split("\\s+");
			String action = args[0];
			System.out.println(action);
			System.out.println(args.length);
			switch(action) {
				case "item":
					if (args.length == 3) {
						c.getItems().addItem(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
					} else if (args.length == 2) {
						c.getItems().addItem(Integer.parseInt(args[1]), 1);
					} else {
						c.sendMessage("Usage: '/item item_id quantity' or /item item_id for 1 item");
					}
					break;
				case "tele":
					if (args.length == 3) {
						c.teleportToX = Integer.parseInt(args[1]);
						c.teleportToY = Integer.parseInt(args[2]);
					} else if (args.length == 1) {
						c.teleportToX = Settings.START_LOCATION_X;
						c.teleportToY = Settings.START_LOCATION_Y;
					} else {
						c.sendMessage("Usage: '/tele x y z' or /tele for home");
					}
					break;
				case "xy":
					c.sendMessage("X: " + c.currentX + " Y: " + c.currentY);
					break;
			}
		}
	}
}
