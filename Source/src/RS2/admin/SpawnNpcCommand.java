package RS2.admin;
import RS2.model.player.Client;
import RS2.model.npc.NPC;
import RS2.GameEngine;
import RS2.db.DatabaseManager;
import RS2.model.npc.NPCHandler;

public class SpawnNpcCommand extends ServerCommand {
    static {
        register("npcadd", new SpawnNpcCommand());
    }

    public String execute(Client c, String[] args) {
        if (args.length < 1 || args.length == 2) {
            return "Usage: '/npcadd npc_id x y' or /npcadd npc_id for current location";
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
        saveSpawnedNPC(npc);
        return "NPC " + npcId + " spawned at (" + x + ", " + y + ")";
    }

    public String serverExecute(String[] args) {
        if (args.length < 3) {
            return "Usage: '/npcadd npc_id x y'";
        }
        int npcId = Integer.parseInt(args[0]);
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        NPC npc = new NPC(npcId, x, y, 0, 0, 100, 0, 0, "");
        saveSpawnedNPC(npc);
        return "NPC " + npcId + " spawned at (" + x + ", " + y + ")";
    }

    private void saveSpawnedNPC(NPC npc) {
        GameEngine.pendingActions.add(() -> {
            GameEngine.npcHandler.newNPC(npc);
            try {
                DatabaseManager.getInstance().saveSpawnedNPC(npc);
            } catch (Exception e) {
                System.err.println("Error saving spawned NPC: " + e.getMessage());
            }
        });
    }
}
