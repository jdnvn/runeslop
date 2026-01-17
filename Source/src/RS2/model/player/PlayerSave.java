package RS2.model.player;
import RS2.db.PlayerRecord;
import RS2.util.Misc;

public class PlayerSave {
	/**
	 * Loading
	 **/
	public static int loadGame(Client p, String playerName, String playerPass) {
		PlayerRecord player = p.database.getPlayer(playerName);

		if (player.loadError) {
			Misc.println(playerName + ": error loading game.");
			return 3;
		}

		if (!player.exists) {
			Misc.println(playerName + ": character not found, creating new player.");
			p.newPlayer = true;
			if (p.database.savePlayer(p)) {
				PlayerRecord newPlayerRecord = p.database.getPlayer(playerName);
				p.playerRecordId = newPlayerRecord.id;
				Misc.println(playerName + ": new player saved with ID " + p.playerRecordId);
			} else {
				Misc.println(playerName + ": failed to create new player record.");
				p.playerRecordId = -1;
				return 3;
			}
			return 0;
		}

		if (playerPass.equalsIgnoreCase(player.password)
			|| Misc.basicEncrypt(playerPass).equals(player.password)) {
			System.out.println("Password matches for player " + playerName);
		} else {
			return 3;
		}

		p.playerRecordId = player.id;

		// TODO: this is just temporary, hoping to use the player object
		// within the client class to separate concerns
		p.heightLevel = player.heightLevel;
		p.teleportToX = player.teleportToX;
		p.teleportToY = player.teleportToY;
		p.playerRights = player.playerRights;
		p.crystalBowArrowCount = player.crystalBowArrowCount;
		p.skullTimer = player.skullTimer;
		p.playerMagicBook = player.playerMagicBook;
		p.specAmount = player.specAmount;
		p.teleBlockDelay = player.teleBlockDelay;
		p.teleBlockLength = player.teleBlockLength;
		p.autoRet = player.autoRet;
		p.accountFlagged = player.accountFlagged;

		for (int i = 0; i < p.playerEquipment.length; i++) {
			p.playerEquipment[i] = player.playerEquipment[i];
			p.playerEquipmentN[i] = player.playerEquipmentN[i];
		}
		for (int i = 0; i < p.playerAppearance.length; i++) {
			p.playerAppearance[i] = player.playerAppearance[i];
		}
		for (int i = 0; i < p.playerLevel.length; i++) {
			p.playerLevel[i] = player.playerLevel[i];
			p.playerXP[i] = player.playerXP[i];
		}
		for (int i = 0; i < p.playerItems.length; i++) {
			p.playerItems[i] = player.playerItems[i];
			p.playerItemsN[i] = player.playerItemsN[i];
		}
		for (int i = 0; i < p.bankItems.length; i++) {
			p.bankItems[i] = player.bankItems[i];
			p.bankItemsN[i] = player.bankItemsN[i];
		}
		for (int i = 0; i < p.friends.length; i++) {
			p.friends[i] = player.friends[i];
		}
		for (int i = 0; i < p.ignores.length; i++) {
			p.ignores[i] = player.ignores[i];
		}

		return 13;
	}

	/**
	 * Saving
	 **/
	public static boolean saveGame(Client p) {
		return p.database.savePlayer(p);
	}
}
