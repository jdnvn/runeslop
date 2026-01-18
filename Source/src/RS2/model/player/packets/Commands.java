package RS2.model.player.packets;

import RS2.Settings;
import RS2.GameEngine;
import RS2.model.player.Client;
import RS2.model.player.PacketType;
import RS2.util.Misc;
import RS2.model.npc.NPCHandler;
import RS2.model.npc.NPC;
import RS2.db.DatabaseManager;
import RS2.model.player.PlayerHandler;
import RS2.model.player.Player;

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
					c.sendMessage("X: " + c.absX + " Y: " + c.absY);
					break;
				case "npc":
					int npcId;
					int x;
					int y;
					int height;
					int walk;
					int hp;
					int maxhit;
					int attack;
					int defence;
					boolean attackPlayer;
					boolean headIcon;
					if (args.length == 4) {
						npcId = Integer.parseInt(args[1]);
						x = Integer.parseInt(args[2]);
						y = Integer.parseInt(args[3]);
						height = 0;
						walk = 0;
						hp = 100; // Default HP
						maxhit = 0;
						attack = 0;
						defence = 0;
						attackPlayer = false;
						headIcon = false;
					} else if (args.length == 2) {
						npcId = Integer.parseInt(args[1]);
						x = c.absX;
						y = c.absY;
						height = 0;
						walk = 0;
						hp = 100; // Default HP
						maxhit = 0;
						attack = 0;
						defence = 0;
						attackPlayer = false;
						headIcon = false;
					} else {
						c.sendMessage("Usage: '/npc npc_id x y' or /npc npc_id for current location");
						return;
					}
					NPC npc = new NPC(npcId, x, y, height, walk, maxhit, attack, defence, "");
					GameEngine.npcHandler.spawnNpc2(npcId, x, y, height, walk, hp, maxhit, attack, defence);
					try {
						DatabaseManager.getInstance().saveSpawnedNPC(npc);
						c.sendMessage("NPC spawned and saved");
					} catch (Exception e) {
						c.sendMessage("NPC spawned but failed to save");
					}
					break;
				case "maxed":
					for (int i = 0; i < 25; i++) {
						c.playerLevel[i] = 99;
						c.playerXP[i] = 2147000000;
						c.getPA().refreshSkill(i);
						c.sendMessage("You're welcome :)");
					}
					break;
				case "setlvl":
					if (args.length == 3) {
						int skillId = Integer.parseInt(args[1]);
						int level = Integer.parseInt(args[2]);
						c.playerLevel[skillId] = level;
						c.playerXP[skillId] = c.getPA().getXPForLevel(level);
						c.getPA().refreshSkill(skillId);
						c.sendMessage("Level set to " + level);
					}
					break;
				// case "promote":
				// 	if (args.length == 3) {
				// 		String playerName = args[1];
				// 		int rights = Integer.parseInt(args[2]);
				// 		for (Player player : PlayerHandler.players) {
				// 			if (player != null && player.playerName.equalsIgnoreCase(playerName)) {
				// 				DatabaseManager.getInstance().updatePlayerRights(player.playerRecordId, rights);
				// 				player.sendMessage("You have been promoted to " + rights + " rights");
				// 				break;
				// 			}
				// 		}
				// 		c.sendMessage("Player not found");
				// 	}
			}
		}
	}
}
