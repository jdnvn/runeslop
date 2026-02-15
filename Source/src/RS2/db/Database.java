package RS2.db;
import RS2.model.player.Player;
import RS2.model.npc.NPCList;
import RS2.model.npc.NPC;
import RS2.model.item.ItemList;
import RS2.model.object.Objects;

public interface Database {
    public abstract PlayerRecord getPlayer(String playerName);

    public abstract boolean savePlayer(Player player);

    public abstract NPCList[] getAllNPCs() throws Exception;

    public abstract NPC[] getAllSpawnedNPCs() throws Exception;

    public abstract void saveSpawnedNPC(NPC npc) throws Exception;

    public abstract NPCList[] searchNPCsByName(String name) throws Exception;

    // Item methods
    public abstract ItemList[] getAllItems() throws Exception;

    public abstract void saveItem(ItemList item) throws Exception;

    public abstract void loadItemsFromConfig() throws Exception;

    public abstract ItemList[] searchItemsByName(String name) throws Exception;

    // Object methods
    public abstract Objects[] getAllGlobalObjects() throws Exception;

    public abstract void saveGlobalObject(Objects object) throws Exception;

    public abstract void loadObjectsFromConfig() throws Exception;

    public abstract Objects[] searchObjectsById(int objectId) throws Exception;
}
