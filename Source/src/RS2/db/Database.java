package RS2.db;
import RS2.model.player.Player;

public interface Database {
    public abstract PlayerRecord getPlayer(String playerName);

    public abstract boolean savePlayer(Player player);
}
