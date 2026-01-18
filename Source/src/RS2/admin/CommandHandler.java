package RS2.admin;
import RS2.model.player.Client;
import java.util.Arrays;

public class CommandHandler {
    public static String executeCommand(String commandString, Client client) {
        String[] commandParts = commandString.split("\\s+");
        if (commandParts.length < 1) {
            return "Invalid command";
        }
        String name = commandParts[0];
        Command command = Command.getCommand(name);
        String[] args;
        if (commandParts.length > 1) {
            args = Arrays.copyOfRange(commandParts, 1, commandParts.length);
        } else {
            args = new String[0];
        }
        try {
            return command.execute(client, args);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing command: " + e.getMessage();
        }
    }    

    public static String executeServerCommand(String commandString) {
        String[] commandParts = commandString.split("\\s+");
        if (commandParts.length < 1) {
            return "Invalid command";
        }
        String name = commandParts[0];
        ServerCommand command = Command.getServerCommand(name);
        if (command == null) {
            return "Command not found: " + name;
        }
        String[] args;
        if (commandParts.length > 1) {
            args = Arrays.copyOfRange(commandParts, 1, commandParts.length);
        } else {
            args = new String[0];
        }
        return command.serverExecute(args);
    }
}
