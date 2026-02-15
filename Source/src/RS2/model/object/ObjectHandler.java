package RS2.model.object;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import RS2.db.Database;
import RS2.db.DatabaseManager;
import RS2.model.player.Client;
import RS2.model.player.Player;
import RS2.model.player.PlayerHandler;
import RS2.util.Misc;

/**
 * @author Sanity
 */
@SuppressWarnings("all")
public class ObjectHandler {

	public List<Objects> globalObjects = new ArrayList<Objects>();
	private Database database = DatabaseManager.getInstance();

	public ObjectHandler() {
		// Load objects from database (which automatically imports from config if empty)
		loadObjectsFromDatabase();
	}
	
	/**
	 * Loads global objects from database
	 */
	public boolean loadObjectsFromDatabase() {
		try {
			Objects[] dbObjects = database.getAllGlobalObjects();
			int count = 0;
			for (Objects obj : dbObjects) {
				if (obj != null) {
					globalObjects.add(obj);
					count++;
				}
			}
			Misc.println("Loaded " + count + " global objects from database into ObjectHandler");
			return true;
		} catch (Exception e) {
			Misc.println("Error loading objects from database, falling back to config file");
			e.printStackTrace();
			return loadGlobalObjects("./Data/cfg/global-objects.cfg");
		}
	}
	
	/**
	 * Force reload objects from config file into database
	 */
	public void reloadObjectsFromConfig() {
		try {
			globalObjects.clear();
			database.loadObjectsFromConfig();
			loadObjectsFromDatabase();
			Misc.println("Reloaded objects from config into database");
		} catch (Exception e) {
			Misc.println("Error reloading objects from config");
			e.printStackTrace();
		}
	}
	
	/**
	 * Save a new global object to database
	 */
	public void saveObjectToDatabase(Objects object) {
		try {
			database.saveGlobalObject(object);
			Misc.println("Saved object " + object.getObjectId() + " to database");
		} catch (Exception e) {
			Misc.println("Error saving object to database");
			e.printStackTrace();
		}
	}

	/**
	 * Adds object to list
	 **/
	public void addObject(Objects object) {
		globalObjects.add(object);
	}

	/**
	 * Removes object from list
	 **/
	public void removeObject(Objects object) {
		globalObjects.remove(object);
	}

	/**
	 * Does object exist
	 **/
	public Objects objectExists(int objectX, int objectY, int objectHeight) {
		for (Objects o : globalObjects) {
			if (o.getObjectX() == objectX && o.getObjectY() == objectY
					&& o.getObjectHeight() == objectHeight) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Update objects when entering a new region or logging in
	 **/
	public void updateObjects(Client c) {
		for (Objects o : globalObjects) {
			if (c != null) {
				if (c.heightLevel == o.getObjectHeight() && o.objectTicks == 0) {
					if (c.distanceToPoint(o.getObjectX(), o.getObjectY()) <= 60) {
						c.getPA().object(o.getObjectId(), o.getObjectX(),
								o.getObjectY(), o.getObjectFace(),
								o.getObjectType());
					}
				}
			}
		}
		if (c.distanceToPoint(2961, 3389) <= 60) {
			c.getPA().object(6552, 2961, 3389, -1, 10);
		}
	}

	/**
	 * Creates the object for anyone who is within 60 squares of the object
	 **/
	public void placeObject(Objects o) {
		for (Player p : PlayerHandler.players) {
			if (p != null) {
				Client person = (Client) p;
				if (person != null) {
					if (person.heightLevel == o.getObjectHeight()
							&& o.objectTicks == 0) {
						if (person.distanceToPoint(o.getObjectX(),
								o.getObjectY()) <= 60) {
							removeAllObjects(o);
							globalObjects.add(o);
							person.getPA().object(o.getObjectId(),
									o.getObjectX(), o.getObjectY(),
									o.getObjectFace(), o.getObjectType());
						}
					}
				}
			}
		}
	}

	public void removeAllObjects(Objects o) {
		for (Objects s : globalObjects) {
			if (s.getObjectX() == o.objectX && s.getObjectY() == o.objectY
					&& s.getObjectHeight() == o.getObjectHeight()) {
				globalObjects.remove(s);
				break;
			}
		}
	}

	public void process() {
		for (int j = 0; j < globalObjects.size(); j++) {
			if (globalObjects.get(j) != null) {
				Objects o = globalObjects.get(j);
				if (o.objectTicks > 0) {
					o.objectTicks--;
				}
				if (o.objectTicks == 1) {
					Objects deleteObject = objectExists(o.getObjectX(),
							o.getObjectY(), o.getObjectHeight());
					removeObject(deleteObject);
					o.objectTicks = 0;
					placeObject(o);
					removeObject(o);
				}
			}

		}
		/*
		 * for(Objects o : globalObjects) { if(o.objectTicks > 0) {
		 * o.objectTicks--; } if(o.objectTicks == 1) { Objects deleteObject =
		 * objectExists(o.getObjectX(), o.getObjectY(), o.getObjectHeight());
		 * if(deleteObject != null) { removeObject(deleteObject); }
		 * o.objectTicks = 0; placeObject(o); removeObject(o); if
		 * (isObelisk(o.objectId)) { int index = getObeliskIndex(o.objectId); if
		 * (activated[index]) { activated[index] = false;
		 * teleportObelisk(index); } } break; } }
		 */
	}

	public boolean loadGlobalObjects(String fileName) {
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader objectFile = null;
		try {
			objectFile = new BufferedReader(new FileReader("./" + fileName));
		} catch (FileNotFoundException fileex) {
			Misc.println(fileName + ": file not found.");
			return false;
		}
		try {
			line = objectFile.readLine();
		} catch (IOException ioexception) {
			Misc.println(fileName + ": error loading file.");
			return false;
		}
		while (EndOfFile == false && line != null) {
			line = line.trim();
			int spot = line.indexOf("=");
			if (spot > -1) {
				token = line.substring(0, spot);
				token = token.trim();
				token2 = line.substring(spot + 1);
				token2 = token2.trim();
				token2_2 = token2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token3 = token2_2.split("\t");
				if (token.equals("object")) {
					Objects object = new Objects(Integer.parseInt(token3[0]),
							Integer.parseInt(token3[1]),
							Integer.parseInt(token3[2]),
							Integer.parseInt(token3[3]),
							Integer.parseInt(token3[4]),
							Integer.parseInt(token3[5]), 0);
					addObject(object);
				}
			} else {
				if (line.equals("[ENDOFOBJECTLIST]")) {
					try {
						objectFile.close();
					} catch (IOException ioexception) {
					}
					return true;
				}
			}
			try {
				line = objectFile.readLine();
			} catch (IOException ioexception1) {
				EndOfFile = true;
			}
		}
		try {
			objectFile.close();
		} catch (IOException ioexception) {
		}
		return false;
	}
}