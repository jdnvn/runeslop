package RS2.admin;
import RS2.model.player.Client;
import RS2.model.player.PlayerHandler;
import java.util.Arrays;

public class ItemCommand extends ServerCommand {
    static {
        register("item", new ItemCommand());
    }

    public String execute(Client c, String[] args) {
        if (args.length < 2)
            return "Usage: '/item item_id quantity' or /item item_id for 1 item";

        int itemId = Integer.parseInt(args[0]);

        if (args.length < 2) {
            c.getItems().addItem(itemId, 1);
            return "Gave 1x item " + itemId + " to " + c.playerName;
        }

        int amount = Integer.parseInt(args[1]);
        System.out.println("Amount: " + amount);
        
        if (args.length <= 3) {
            c.getItems().addItem(itemId, amount);
            return "Gave " + amount + "x item " + itemId + " to " + c.playerName;
        }

        String playerName = args[2];

        Client target = PlayerHandler.findPlayer(playerName);
        if (target != null) {
            target.getItems().addItem(itemId, amount);
            return "Gave " + amount + "x item " + itemId + " to " + playerName;
        }

        return "Player not found: " + playerName;
    }

    public String serverExecute(String[] args) {
        System.out.println("Server Execute");
        if (args.length < 3)
            return "Usage: '/item player_name item_id quantity or /item player_name item_id for 1 item";
        String playerName = args[0];
        int itemId = Integer.parseInt(args[1]);
        int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        Client target = PlayerHandler.findPlayer(playerName);
        if (target != null) {
            target.getItems().addItem(itemId, amount);
            return "Gave " + amount + "x item " + itemId + " to " + playerName;
        }
        return "Player not found: " + playerName;
    }
}
