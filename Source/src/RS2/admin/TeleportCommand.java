package RS2.admin;
import RS2.model.player.Client;
import RS2.model.player.PlayerHandler;
import RS2.Settings;

public class TeleportCommand extends Command {
    static {
        register("tele", new TeleportCommand());
    }

    public String execute(Client c, String[] args) {
        if (args.length > 2)
            return "Usage: '/tele x y' or /tele for home";

        if (args.length >= 2) {
            c.teleportToX = Integer.parseInt(args[0]);
            c.teleportToY = Integer.parseInt(args[1]);
            return "Teleported to " + c.teleportToX + ", " + c.teleportToY;
        }

        c.teleportToX = Settings.START_LOCATION_X;
        c.teleportToY = Settings.START_LOCATION_Y;
        return "Teleported to home";
    }
}
