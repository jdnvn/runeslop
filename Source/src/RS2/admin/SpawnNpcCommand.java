package RS2.admin;
import RS2.model.player.Client;
import RS2.model.npc.NPC;
import RS2.GameEngine;
import RS2.db.DatabaseManager;

public class SpawnNpcCommand extends Command {
    static {
        register("tele", new TeleportCommand());
    }

    public String execute(Client c, String[] args) {
        if (args.length < 1 || args.length == 2) {
            return "Usage: '/npc npc_id x y' or /npc npc_id for current location";
        }

        int npcId = Integer.parseInt(args[0]);
        int x;
        int y;
        int height = 0;
        int walk = 0;
        int hp = 100;
        int maxhit = 0;
        int attack = 0;
        int defence = 0;
        if (args.length == 3) {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
        } else {
            x = c.absX;
            y = c.absY;
        }
        NPC npc = new NPC(npcId, x, y, height, walk, maxhit, attack, defence, "");
        GameEngine.npcHandler.spawnNpc2(npcId, x, y, height, walk, hp, maxhit, attack, defence);
        try {
            DatabaseManager.getInstance().saveSpawnedNPC(npc);
            return "NPC spawned and saved";
        } catch (Exception e) {
            return "NPC spawned but failed to save: " + e.getMessage();
        }
    }
}
