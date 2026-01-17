package RS2.model.npc;

public class NPCList {
	public int npcId;
	public String npcName;
	public int npcCombat;
	public int npcHealth;

	public NPCList(int _npcId, String _npcName, int _npcCombat, int _npcHealth) {
		npcId = _npcId;
		npcName = _npcName;
		npcCombat = _npcCombat;
		npcHealth = _npcHealth;
	}

	public NPCList(int _npcId) {
		npcId = _npcId;
	}
}
