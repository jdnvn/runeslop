package RS2.model.player.packets;

import RS2.model.player.Client;
import RS2.model.player.PacketType;

@SuppressWarnings("all")
public class ItemClick3 implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int itemId11 = c.getInStream().readSignedWordBigEndianA();
		int itemId1 = c.getInStream().readSignedWordA();
		int itemId = c.getInStream().readSignedWordA();
		switch (itemId) {
		}
	}
}