package RS2.db;
import RS2.model.player.Player;
import RS2.model.npc.NPCList;
import RS2.model.npc.NPC;

public interface Database {
    public abstract PlayerRecord getPlayer(String playerName);

    public abstract boolean savePlayer(Player player);

    public abstract NPCList[] getAllNPCs() throws Exception;

    public abstract NPC[] getAllSpawnedNPCs() throws Exception;

    public abstract void saveSpawnedNPC(NPC npc) throws Exception;
}
