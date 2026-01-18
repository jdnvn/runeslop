package RS2.admin;
import RS2.model.player.Client;
import RS2.model.player.PlayerHandler;

public class CoordinatesCommand extends Command {
    static {
        register("xy", new CoordinatesCommand());
    }

    public String execute(Client c, String[] args) {
        if (args.length > 1) {
            return "Usage: '/xy' or '/xy player_name'";
        }

        if (args.length == 1) {
            String playerName = args[0];
            Client target = PlayerHandler.findPlayer(playerName);
            if (target != null) {
                return playerName + "'s coordinates: (" + target.absX + ", " + target.absY + ")";
            }
            return "Player not found: " + playerName;
        }

        return "(" + c.absX + ", " + c.absY + ")";
    }
}
