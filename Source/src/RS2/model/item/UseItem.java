package RS2.model.item;

import RS2.model.player.Client;

public class UseItem {
	
	public static void ItemonObject(Client c, int objectID, int objectX,
			int objectY, int itemId) {
		if (!c.getItems().playerHasItem(itemId, 1))
			return;
		switch (objectID) {
		}
	}

	public static void ItemonItem(Client c, int itemUsed, int useWith) {
		switch (itemUsed) {
		}
	}

	public static void ItemonNpc(Client c, int itemId, int npcId, int slot) {
		switch (itemId) {
		}
	}
}