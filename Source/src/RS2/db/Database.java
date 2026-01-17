package RS2.db;
import RS2.model.player.Player;
import RS2.model.npc.NPCList;

public interface Database {
    public abstract PlayerRecord getPlayer(String playerName);

    public abstract boolean savePlayer(Player player);

    public abstract NPCList[] getAllNPCs() throws Exception;
}
