package RS2.model.item;

import RS2.model.player.Client;
import RS2.model.player.PacketType;


/**
 * Wear Item
 **/
@SuppressWarnings("all")
public class WearItem implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.wearId = c.getInStream().readUnsignedWord();
		c.wearSlot = c.getInStream().readUnsignedWordA();
		c.interfaceId = c.getInStream().readUnsignedWordA();
		
		int oldCombatTimer = c.attackTimer;
		if (c.playerIndex > 0 || c.npcIndex > 0)
			c.getCombat().resetPlayerAttack();
		c.getItems().wearItem(c.wearId, c.wearSlot);
	}
}