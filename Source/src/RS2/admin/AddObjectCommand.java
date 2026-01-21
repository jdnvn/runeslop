package RS2.admin;

import RS2.GameEngine;
import RS2.model.player.Client;
import java.util.Arrays;

// TODO: this is broken, need to make object creation thread safe
public class AddObjectCommand extends ServerCommand {
    static {
        register("objadd", new AddObjectCommand());
    }

    @Override
    public String execute(Client c, String[] args) {
        if (args.length < 1) {
            return "Usage: '/objadd obj_id' or '/objadd obj_id x y height face' | Face: 0=North, 1=East, 2=South, 3=West";
        }
        
        int objId = Integer.parseInt(args[0]);
        int objX;
        int objY;
        int objHeight;
        int objFace;
        int objType = 10; // Default type (wall/building)
        
        if (args.length >= 5) {
            // Full specification
            objX = Integer.parseInt(args[1]);
            objY = Integer.parseInt(args[2]);
            objHeight = Integer.parseInt(args[3]);
            objFace = Integer.parseInt(args[4]);
        } else {
            objX = c.absX;
            objY = c.absY;
            objHeight = c.heightLevel;
            objFace = 0; // North facing
        }
        
        c.getPA().object(objId, objX, objY, objFace, objType);
        return "Object " + objId + " spawned at (" + objX + ", " + objY + ") height=" + objHeight + " face=" + objFace;
    }

    public String serverExecute(String[] args) {
        if (args.length < 4) {
            return "Usage: '/objadd obj_id x y face' | Face: 0=North, 1=East, 2=South, 3=West";
        }
        int objId = Integer.parseInt(args[0]);
        int objX = Integer.parseInt(args[1]);
        int objY = Integer.parseInt(args[2]);
        int objFace = Integer.parseInt(args[3]);
        GameEngine.pendingActions.add(() -> {
            new RS2.model.object.Object(objId, objX, objY, 0, objFace, 10, 0, 0, 0, 0, 0);
        });
        return "Object " + objId + " spawned at (" + objX + ", " + objY + ") face=" + objFace;
    }
}
