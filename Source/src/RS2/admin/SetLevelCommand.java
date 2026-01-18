package RS2.admin;

import RS2.model.player.Client;
import RS2.model.player.PlayerHandler;

public class SetLevelCommand extends ServerCommand {
    static {
        register("setlevel", new SetLevelCommand());
    }

    @Override
    public String execute(Client c, String[] args) {
        if (args.length < 2) {
            return "Usage: '/setlevel skill_id level' or '/setlevel player skill_id level'";
        }
        
        Client target = c;
        int skill;
        int level;
        
        if (args.length >= 3) {
            target = PlayerHandler.findPlayer(args[0]);
            if (target == null) {
                return "Player not found: " + args[0];
            }
            skill = Integer.parseInt(args[1]);
            level = Integer.parseInt(args[2]);
        } else {
            skill = Integer.parseInt(args[0]);
            level = Integer.parseInt(args[1]);
        }
        
        if (skill < 0 || skill >= 25) {
            return "Invalid skill ID (must be 0-24)";
        }
        if (level < 1 || level > 99) {
            return "Invalid level (must be 1-99)";
        }
        
        target.playerLevel[skill] = level;
        target.playerXP[skill] = target.getPA().getXPForLevel(level);
        target.updateRequired = true;
        target.setAppearanceUpdateRequired(true);
        
        if (target == c) {
            return "Skill " + skill + " set to level " + level;
        } else {
            return "Set " + target.playerName + "'s skill " + skill + " to level " + level;
        }
    }

    public String serverExecute(String[] args) {
        if (args.length < 3) {
            return "Usage: '/setlevel player skill_id level'";
        }
        String playerName = args[0];
        int skill = Integer.parseInt(args[1]);
        int level = Integer.parseInt(args[2]);
        Client target = PlayerHandler.findPlayer(playerName);
        if (target == null) {
            return "Player not found: " + playerName;
        }
        target.playerLevel[skill] = level;
        target.playerXP[skill] = target.getPA().getXPForLevel(level);
        target.updateRequired = true;
        target.setAppearanceUpdateRequired(true);
        return "Set " + target.playerName + "'s skill " + skill + " to level " + level;
    }
}
