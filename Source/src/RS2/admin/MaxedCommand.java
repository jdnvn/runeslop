package RS2.admin;

import RS2.model.player.Client;
import RS2.model.player.PlayerHandler;

public class MaxedCommand extends ServerCommand {
    static {
        register("maxed", new MaxedCommand());
    }

    @Override
    public String execute(Client c, String[] args) {
        Client target = c;
        
        if (args.length >= 1) {
            target = PlayerHandler.findPlayer(args[0]);
            if (target == null) {
                return "Player not found: " + args[0];
            }
        }
        
        for (int i = 0; i < 25; i++) {
            target.playerLevel[i] = 99;
            target.playerXP[i] = 200000000;
        }
        target.updateRequired = true;
        target.setAppearanceUpdateRequired(true);
        
        if (target == c) {
            return "All skills set to 99!";
        } else {
            return "Maxed all skills for " + target.playerName;
        }
    }

    public String serverExecute(String[] args) {
        if (args.length < 1) {
            return "Usage: '/maxed player_name'";
        }
        String playerName = args[0];
        Client target = PlayerHandler.findPlayer(playerName);
        if (target == null) {
            return "Player not found: " + playerName;
        }
        for (int i = 0; i < 25; i++) {
            target.playerLevel[i] = 99;
            target.playerXP[i] = 200000000;
        }
        target.updateRequired = true;
        target.setAppearanceUpdateRequired(true);
        return "Maxed all skills for " + playerName;
    }
}
