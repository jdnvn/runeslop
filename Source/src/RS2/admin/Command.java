package RS2.admin;

import java.util.HashMap;
import java.util.Map;

import RS2.model.player.Client;

public abstract class Command {
    private static final Map<String, Command> REGISTRY = new HashMap<>();

    static {
        new ItemCommand();
        new TeleportCommand();
        new SpawnNpcCommand();
        new CoordinatesCommand();
        new AddObjectCommand();
        new MaxedCommand();
        new SetLevelCommand();
    }

    protected static void register(String name, Command command) {
        REGISTRY.put(name, command);
    }

    public static Command getCommand(String name) {
        return REGISTRY.get(name);
    }

    public static ServerCommand getServerCommand(String name) {
        try {
            return (ServerCommand) REGISTRY.get(name);
        } catch (ClassCastException e) {
            System.out.println("Command " + name + " is not a ServerCommand");
            return null;
        }
    }

    public abstract String execute(Client client, String[] args);
}
