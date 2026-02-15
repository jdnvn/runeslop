package RS2.db;
import RS2.model.player.Player;
import RS2.model.player.PlayerHandler;
import RS2.model.npc.NPCList;
import RS2.model.npc.NPCHandler;
import RS2.model.npc.NPC;
import RS2.model.item.ItemList;
import RS2.model.object.Objects;
import RS2.Settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import RS2.util.Misc;

public class FileDatabase implements Database {
    public static final String CHARACTERS_PATH = "./Data/characters/";
    public static final String NPC_FILENAME = "./Data/CFG/npc.cfg";
    public static final String SPAWNED_NPCS_FILENAME = "./Data/CFG/spawn-config.cfg";
    public static final String ITEMS_FILENAME = "./Data/cfg/item.cfg";
    public static final String GLOBAL_OBJECTS_FILENAME = "./Data/cfg/global-objects.cfg";

    FileDatabase() {}

    public PlayerRecord getPlayer(String playerName) {
		String line = "";
		String token = "";
		String token2 = "";
		String[] token3 = new String[3];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		boolean File1 = false;

        PlayerRecord p = new PlayerRecord(playerName);

        try {
			characterfile = new BufferedReader(new FileReader(CHARACTERS_PATH + playerName + ".txt"));
			File1 = true;
		} catch (FileNotFoundException fileex1) {
		}

        if (File1) {
            p.exists = true;
		} else {
			Misc.println(playerName + ": character file not found.");
			p.newPlayer = false;
			return p;
		}

		try {
			line = characterfile.readLine();
		} catch (IOException ioexception) {
			Misc.println(playerName + ": error loading file.");
            p.loadError = true;
			return p;
		}

        while (EndOfFile == false && line != null) {
            try {
                line = line.trim();
                int spot = line.indexOf("=");
                if (spot > -1) {
                    token = line.substring(0, spot);
                    token = token.trim();
                    token2 = line.substring(spot + 1);
                    token2 = token2.trim();
                    token3 = token2.split("\t");
                    switch (ReadMode) {
                    case 1:
                        if (token.equals("character-password")) {
                            p.password = token2;
                        }
                        break;
                    case 2:
                        if (token.equals("character-height")) {
                            p.heightLevel = Integer.parseInt(token2);
                        } else if (token.equals("character-posx")) {
                            p.teleportToX = (Integer.parseInt(token2) <= 0 ? 3210
                                    : Integer.parseInt(token2));
                        } else if (token.equals("character-posy")) {
                            p.teleportToY = (Integer.parseInt(token2) <= 0 ? 3424
                                    : Integer.parseInt(token2));
                        } else if (token.equals("character-rights")) {
                            p.playerRights = Integer.parseInt(token2);
                        } else if (token.equals("crystal-bow-shots")) {
                            p.crystalBowArrowCount = Integer.parseInt(token2);
                        } else if (token.equals("skull-timer")) {
                            p.skullTimer = Integer.parseInt(token2);
                        } else if (token.equals("magic-book")) {
                            p.playerMagicBook = Integer.parseInt(token2);
                        } else if (token.equals("special-amount")) {
                            p.specAmount = Double.parseDouble(token2);
                        } else if (token.equals("teleblock-length")) {
                            p.teleBlockDelay = System.currentTimeMillis();
                            p.teleBlockLength = Integer.parseInt(token2);
                        } else if (token.equals("autoRet")) {
                            p.autoRet = Integer.parseInt(token2);
                        } else if (token.equals("flagged")) {
                            p.accountFlagged = Boolean.parseBoolean(token2);
                        }
                        break;
                    case 3:
                        if (token.equals("character-equip")) {
                            p.playerEquipment[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[1]);
                            p.playerEquipmentN[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[2]);
                        }
                        break;
                    case 4:
                        if (token.equals("character-look")) {
                            p.playerAppearance[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[1]);
                        }
                        break;
                    case 5:
                        if (token.equals("character-skill")) {
                            p.playerLevel[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[1]);
                            p.playerXP[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[2]);
                        }
                        break;
                    case 6:
                        if (token.equals("character-item")) {
                            p.playerItems[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[1]);
                            p.playerItemsN[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[2]);
                        }
                        break;
                    case 7:
                        if (token.equals("character-bank")) {
                            p.bankItems[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[1]);
                            p.bankItemsN[Integer.parseInt(token3[0])] = Integer
                                    .parseInt(token3[2]);
                        }
                        break;
                    case 8:
                        if (token.equals("character-friend")) {
                            p.friends[Integer.parseInt(token3[0])] = Long
                                    .parseLong(token3[1]);
                        }
                        break;
                    case 9:
                        /*
                        * if (token.equals("character-ignore")) {
                        * ignores[Integer.parseInt(token3[0])] =
                        * Long.parseLong(token3[1]); }
                        */
                        break;
                    }
                } else {
                    if (line.equals("[ACCOUNT]")) {
                        ReadMode = 1;
                    } else if (line.equals("[CHARACTER]")) {
                        ReadMode = 2;
                    } else if (line.equals("[EQUIPMENT]")) {
                        ReadMode = 3;
                    } else if (line.equals("[LOOK]")) {
                        ReadMode = 4;
                    } else if (line.equals("[SKILLS]")) {
                        ReadMode = 5;
                    } else if (line.equals("[ITEMS]")) {
                        ReadMode = 6;
                    } else if (line.equals("[BANK]")) {
                        ReadMode = 7;
                    } else if (line.equals("[FRIENDS]")) {
                        ReadMode = 8;
                    } else if (line.equals("[IGNORES]")) {
                        ReadMode = 9;
                    } else if (line.equals("[EOF]")) {
                        try {
                            characterfile.close();
                        } catch (IOException ioexception) {
                        }
                        // TODO: need to set something here? I don't think it's handled
                        return p;
                    }
                }
                try {
                    line = characterfile.readLine();
                } catch (IOException ioexception1) {
                    EndOfFile = true;
                }
            } catch (Exception e) {
                Misc.println(playerName + ": error reading line: " + e);
                e.printStackTrace();
                p.loadError = true;
                return p;
            }
		}
		try {
			characterfile.close();
		} catch (IOException ioexception) {
		}

        return p;
    }

    public boolean savePlayer(Player p) {
        if (!p.saveFile || p.newPlayer || !p.saveCharacter) {
			return false;
		}
		if (p.playerName == null || PlayerHandler.players[p.playerId] == null) {
			return false;
		}
		p.playerName = p.playerName2;
		int tbTime = (int) (p.teleBlockDelay - System.currentTimeMillis() + p.teleBlockLength);
		if (tbTime > 300000 || tbTime < 0) {
			tbTime = 0;
		}

		BufferedWriter characterfile = null;
		try {
			characterfile = new BufferedWriter(new FileWriter(CHARACTERS_PATH + p.playerName + ".txt"));

			/* ACCOUNT */
			characterfile.write("[ACCOUNT]", 0, 9);
			characterfile.newLine();
			characterfile.write("character-username = ", 0, 21);
			characterfile.write(p.playerName, 0, p.playerName.length());
			characterfile.newLine();
			characterfile.write("character-password = ", 0, 21);
			// p.playerPass = Misc.basicEncrypt(p.playerPass);
			characterfile.write(p.playerPass, 0, p.playerPass.length());
			// characterfile.write(Misc.basicEncrypt(p.playerPass).toString(),
			// 0, Misc.basicEncrypt(p.playerPass).toString().length());
			characterfile.newLine();
			characterfile.newLine();

			/* CHARACTER */
			characterfile.write("[CHARACTER]", 0, 11);
			characterfile.newLine();
			characterfile.write("character-height = ", 0, 19);
			characterfile.write(Integer.toString(p.heightLevel), 0, Integer
					.toString(p.heightLevel).length());
			characterfile.newLine();
			characterfile.write("character-posx = ", 0, 17);
			characterfile.write(Integer.toString(p.absX), 0,
					Integer.toString(p.absX).length());
			characterfile.newLine();
			characterfile.write("character-posy = ", 0, 17);
			characterfile.write(Integer.toString(p.absY), 0,
					Integer.toString(p.absY).length());
			characterfile.newLine();
			characterfile.write("character-rights = ", 0, 19);
			characterfile.write(Integer.toString(p.playerRights), 0, Integer
					.toString(p.playerRights).length());
			characterfile.newLine();
			characterfile.write("crystal-bow-shots = ", 0, 20);
			characterfile.write(Integer.toString(p.crystalBowArrowCount), 0,
					Integer.toString(p.crystalBowArrowCount).length());
			characterfile.newLine();
			characterfile.write("skull-timer = ", 0, 14);
			characterfile.write(Integer.toString(p.skullTimer), 0, Integer
					.toString(p.skullTimer).length());
			characterfile.newLine();
			characterfile.write("magic-book = ", 0, 13);
			characterfile.write(Integer.toString(p.playerMagicBook), 0, Integer
					.toString(p.playerMagicBook).length());
			characterfile.newLine();
			characterfile.write("special-amount = ", 0, 17);
			characterfile.write(Double.toString(p.specAmount), 0, Double
					.toString(p.specAmount).length());
			characterfile.newLine();
			characterfile.write("teleblock-length = ", 0, 19);
			characterfile.write(Integer.toString(tbTime), 0,
					Integer.toString(tbTime).length());
			characterfile.newLine();
			characterfile.write("autoRet = ", 0, 10);
			characterfile.write(Integer.toString(p.autoRet), 0, Integer
					.toString(p.autoRet).length());
			characterfile.newLine();
			characterfile.write("flagged = ", 0, 10);
			characterfile.write(Boolean.toString(p.accountFlagged), 0, Boolean
					.toString(p.accountFlagged).length());
			characterfile.newLine();

			/* EQUIPMENT */
			characterfile.write("[EQUIPMENT]", 0, 11);
			characterfile.newLine();
			for (int i = 0; i < p.playerEquipment.length; i++) {
				characterfile.write("character-equip = ", 0, 18);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i)
						.length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerEquipment[i]), 0,
						Integer.toString(p.playerEquipment[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerEquipmentN[i]), 0,
						Integer.toString(p.playerEquipmentN[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.newLine();
			}
			characterfile.newLine();

			/* LOOK */
			characterfile.write("[LOOK]", 0, 6);
			characterfile.newLine();
			for (int i = 0; i < p.playerAppearance.length; i++) {
				characterfile.write("character-look = ", 0, 17);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i)
						.length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerAppearance[i]), 0,
						Integer.toString(p.playerAppearance[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* SKILLS */
			characterfile.write("[SKILLS]", 0, 8);
			characterfile.newLine();
			for (int i = 0; i < p.playerLevel.length; i++) {
				characterfile.write("character-skill = ", 0, 18);
				characterfile.write(Integer.toString(i), 0, Integer.toString(i)
						.length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerLevel[i]), 0,
						Integer.toString(p.playerLevel[i]).length());
				characterfile.write("	", 0, 1);
				characterfile.write(Integer.toString(p.playerXP[i]), 0, Integer
						.toString(p.playerXP[i]).length());
				characterfile.newLine();
			}
			characterfile.newLine();

			/* ITEMS */
			characterfile.write("[ITEMS]", 0, 7);
			characterfile.newLine();
			for (int i = 0; i < p.playerItems.length; i++) {
				if (p.playerItems[i] > 0) {
					characterfile.write("character-item = ", 0, 17);
					characterfile.write(Integer.toString(i), 0, Integer
							.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(p.playerItems[i]), 0,
							Integer.toString(p.playerItems[i]).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(p.playerItemsN[i]), 0,
							Integer.toString(p.playerItemsN[i]).length());
					characterfile.newLine();
				}
			}
			characterfile.newLine();

			/* BANK */
			characterfile.write("[BANK]", 0, 6);
			characterfile.newLine();
			for (int i = 0; i < p.bankItems.length; i++) {
				if (p.bankItems[i] > 0) {
					characterfile.write("character-bank = ", 0, 17);
					characterfile.write(Integer.toString(i), 0, Integer
							.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(p.bankItems[i]), 0,
							Integer.toString(p.bankItems[i]).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Integer.toString(p.bankItemsN[i]), 0,
							Integer.toString(p.bankItemsN[i]).length());
					characterfile.newLine();
				}
			}
			characterfile.newLine();

			/* FRIENDS */
			characterfile.write("[FRIENDS]", 0, 9);
			characterfile.newLine();
			for (int i = 0; i < p.friends.length; i++) {
				if (p.friends[i] > 0) {
					characterfile.write("character-friend = ", 0, 19);
					characterfile.write(Integer.toString(i), 0, Integer
							.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write("" + p.friends[i]);
					characterfile.newLine();
				}
			}
			characterfile.newLine();

			/* IGNORES */
			characterfile.write("[IGNORES]", 0, 9);
			characterfile.newLine();
			for (int i = 0; i < p.ignores.length; i++) {
				if (p.ignores[i] > 0) {
					characterfile.write("character-ignore = ", 0, 19);
					characterfile.write(Integer.toString(i), 0, Integer
							.toString(i).length());
					characterfile.write("	", 0, 1);
					characterfile.write(Long.toString(p.ignores[i]), 0, Long
							.toString(p.ignores[i]).length());
					characterfile.newLine();
				}
			}
			characterfile.newLine();
			/* EOF */
			characterfile.write("[EOF]", 0, 5);
			characterfile.newLine();
			characterfile.newLine();
			characterfile.close();
		} catch (IOException ioexception) {
			Misc.println(p.playerName + ": error writing file.");
			ioexception.printStackTrace();
			return false;
		}
		return true;
    }

    public NPCList[] getAllNPCs() throws Exception {
        String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
        NPCList[] npcs = new NPCList[NPCHandler.maxListedNPCs];
		boolean EndOfFile = false;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader(NPC_FILENAME));
		} catch (FileNotFoundException fileex) {
			Misc.println(NPC_FILENAME + ": file not found.");
			throw fileex;
		}
		try {
			line = characterfile.readLine();
		} catch (IOException ioexception) {
			Misc.println(NPC_FILENAME + ": error loading file.");
            characterfile.close();
			throw ioexception;
		}
        int i = 0;
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
				if (token.equals("npc")) {
					npcs[i] = new NPCList(Integer.parseInt(token3[0]), token3[1], Integer.parseInt(token3[2]), Integer.parseInt(token3[3]));
                    i++;
				}
			} else {
				if (line.equals("[ENDOFNPCLIST]")) {
					try {
						characterfile.close();
					} catch (IOException ioexception) {
					}
					return npcs;
				}
			}
			try {
				line = characterfile.readLine();
			} catch (IOException ioexception1) {
				EndOfFile = true;
			}
		}
		try {
			characterfile.close();
		} catch (IOException ioexception) {
		}
		return npcs;
    }

    public NPC[] getAllSpawnedNPCs() throws Exception {
        String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader(SPAWNED_NPCS_FILENAME));
		} catch (FileNotFoundException fileex) {
			Misc.println(SPAWNED_NPCS_FILENAME + ": file not found.");
			throw fileex;
		}
		try {
			line = characterfile.readLine();
		} catch (IOException ioexception) {
			Misc.println(SPAWNED_NPCS_FILENAME + ": error loading file.");
			characterfile.close();
			throw ioexception;
		}
        int i = 0;
        NPC[] spawnedNPCs = new NPC[Settings.MAX_NPCS];
		while (EndOfFile == false && line != null) {
			line = line.trim();
			int spot = line.indexOf("=");
			if (spot > -1) {
				token = line.substring(0, spot);
				token = token.trim();
				token2 = line.substring(spot + 1);
				token2 = token2.trim();
				token2_2 = token2.trim();
				token3 = token2_2.split("\\s+");
				if (token.equals("spawn")) {
					int npcId = Integer.parseInt(token3[0]);
					int spawnX = Integer.parseInt(token3[1]);
					int spawnY = Integer.parseInt(token3[2]);
					int height = Integer.parseInt(token3[3]);
					int walk = Integer.parseInt(token3[4]);
					int maxhit = Integer.parseInt(token3[5]);
					int attack = Integer.parseInt(token3[6]);
					int defence = Integer.parseInt(token3[7]);
					String description = token3.length > 8 ? token3[8] : "";
					spawnedNPCs[i] = new NPC(npcId, spawnX, spawnY, height, walk, maxhit, attack, defence, description);
					i++;
				}
			} else {
				if (line.equals("[ENDOFSPAWNLIST]")) {
					try {
						characterfile.close();
					} catch (IOException ioexception) {
					}
					return spawnedNPCs;
				}
			}
			try {
				line = characterfile.readLine();
			} catch (IOException ioexception1) {
				EndOfFile = true;
			}
		}
		try {
			characterfile.close();
		} catch (IOException ioexception) {
		}
		return spawnedNPCs;
    }

    private NPCList getNPCDetails(int npcId) {
        try {
            NPCList[] allNPCs = getAllNPCs();
            for (NPCList npc : allNPCs) {
                if (npc != null && npc.npcId == npcId) {
                    return npc;
                }
            }
        } catch (Exception e) {
            System.err.println("Error looking up NPC " + npcId);
            e.printStackTrace();
        }
        return null;
    }

	public NPCList[] searchNPCsByName(String name) throws Exception {
		NPCList[] allNPCs = getAllNPCs();
		List<NPCList> matchingNPCs = new ArrayList<>();
		String searchName = name.toLowerCase();
		for (NPCList npc : allNPCs) {
			if (npc != null && npc.npcName != null && npc.npcName.toLowerCase().contains(searchName)) {
				matchingNPCs.add(npc);
			}
		}
		return matchingNPCs.toArray(new NPCList[0]);
	}

    public void saveSpawnedNPC(NPC npc) throws Exception {
        BufferedWriter characterfile = null;
        try {
            characterfile = new BufferedWriter(new FileWriter(SPAWNED_NPCS_FILENAME));
        } catch (IOException ioexception) {
            Misc.println(SPAWNED_NPCS_FILENAME + ": error writing file.");
            ioexception.printStackTrace();
            throw ioexception;
        }
        writeSpawnedNPC(characterfile, npc);
        characterfile.close();
    }

    private void writeSpawnedNPC(BufferedWriter characterfile, NPC npc) throws IOException {
        characterfile.write("spawn = " + npc.npcId + " " + npc.spawnX + " " + npc.spawnY + " " + npc.heightLevel + " " + npc.walkingType + " " + npc.maxHit + " " + npc.attack + " " + npc.defence + " " + npc.description);
        characterfile.newLine();
    }

    // ==================== ITEM METHODS ====================

    /**
     * Get all items from config file
     */
    public ItemList[] getAllItems() throws Exception {
        List<ItemList> itemsList = new ArrayList<>();
        String line = "";
        String token = "";
        String token2 = "";
        String token2_2 = "";
        String[] token3 = new String[20];
        boolean EndOfFile = false;
        BufferedReader characterfile = null;
        
        try {
            characterfile = new BufferedReader(new FileReader(ITEMS_FILENAME));
        } catch (FileNotFoundException fileex) {
            Misc.println(ITEMS_FILENAME + ": file not found.");
            throw fileex;
        }
        
        try {
            line = characterfile.readLine();
        } catch (IOException ioexception) {
            Misc.println(ITEMS_FILENAME + ": error loading file.");
            characterfile.close();
            throw ioexception;
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
                if (token.equals("item")) {
                    int[] Bonuses = new int[12];
                    for (int i = 0; i < 12; i++) {
                        if ((6 + i) < token3.length && token3[(6 + i)] != null) {
                            try {
                                Bonuses[i] = Integer.parseInt(token3[(6 + i)]);
                            } catch (NumberFormatException e) {
                                Bonuses[i] = 0;
                            }
                        } else {
                            break;
                        }
                    }
                    ItemList item = new ItemList(Integer.parseInt(token3[0]));
                    item.itemName = token3[1].replaceAll("_", " ");
                    item.itemDescription = token3[2].replaceAll("_", " ");
                    item.ShopValue = Double.parseDouble(token3[4]);
                    item.LowAlch = Double.parseDouble(token3[4]);
                    item.HighAlch = Double.parseDouble(token3[6]);
                    item.Bonuses = Bonuses;
                    itemsList.add(item);
                }
            } else {
                if (line.equals("[ENDOFITEMLIST]")) {
                    try {
                        characterfile.close();
                    } catch (IOException ioexception) {
                    }
                    System.out.println("Loaded " + itemsList.size() + " items from config file");
                    return itemsList.toArray(new ItemList[0]);
                }
            }
            try {
                line = characterfile.readLine();
            } catch (IOException ioexception1) {
                EndOfFile = true;
            }
        }
        try {
            characterfile.close();
        } catch (IOException ioexception) {
        }
        System.out.println("Loaded " + itemsList.size() + " items from config file");
        return itemsList.toArray(new ItemList[0]);
    }

    /**
     * Save an item to config file (appends to file)
     */
    public void saveItem(ItemList item) throws Exception {
        // For file-based storage, we would need to rewrite the entire file
        // This is a simplified version that just logs what would be saved
        Misc.println("FileDatabase.saveItem: Would save item " + item.itemId + " - " + item.itemName);
        // In a full implementation, you would read all items, add/update this one, and rewrite the file
    }

    /**
     * Reload items from config - for file database this just returns
     */
    public void loadItemsFromConfig() throws Exception {
        // For file database, getAllItems already loads from config
        Misc.println("FileDatabase.loadItemsFromConfig: Items are loaded directly from config file");
    }

    /**
     * Search items by name (case-insensitive, partial match)
     */
    public ItemList[] searchItemsByName(String name) throws Exception {
        ItemList[] allItems = getAllItems();
        List<ItemList> matchingItems = new ArrayList<>();
        String searchName = name.toLowerCase();
        
        for (ItemList item : allItems) {
            if (item != null && item.itemName != null && 
                item.itemName.toLowerCase().contains(searchName)) {
                matchingItems.add(item);
            }
        }
        
        System.out.println("Found " + matchingItems.size() + " items matching '" + name + "'");
        return matchingItems.toArray(new ItemList[0]);
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Get all global objects from config file
     */
    public Objects[] getAllGlobalObjects() throws Exception {
        List<Objects> objectsList = new ArrayList<>();
        String line = "";
        String token = "";
        String token2 = "";
        String token2_2 = "";
        String[] token3 = new String[10];
        boolean EndOfFile = false;
        BufferedReader objectFile = null;
        
        try {
            objectFile = new BufferedReader(new FileReader(GLOBAL_OBJECTS_FILENAME));
        } catch (FileNotFoundException fileex) {
            Misc.println(GLOBAL_OBJECTS_FILENAME + ": file not found.");
            throw fileex;
        }
        
        try {
            line = objectFile.readLine();
        } catch (IOException ioexception) {
            Misc.println(GLOBAL_OBJECTS_FILENAME + ": error loading file.");
            objectFile.close();
            throw ioexception;
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
                    Objects object = new Objects(
                        Integer.parseInt(token3[0]),  // objectId
                        Integer.parseInt(token3[1]),  // x
                        Integer.parseInt(token3[2]),  // y
                        Integer.parseInt(token3[3]),  // height
                        Integer.parseInt(token3[4]),  // face
                        Integer.parseInt(token3[5]),  // type
                        0  // ticks
                    );
                    objectsList.add(object);
                }
            } else {
                if (line.equals("[ENDOFOBJECTLIST]")) {
                    try {
                        objectFile.close();
                    } catch (IOException ioexception) {
                    }
                    System.out.println("Loaded " + objectsList.size() + " global objects from config file");
                    return objectsList.toArray(new Objects[0]);
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
        System.out.println("Loaded " + objectsList.size() + " global objects from config file");
        return objectsList.toArray(new Objects[0]);
    }

    /**
     * Save a global object to config file (appends to file)
     */
    public void saveGlobalObject(Objects object) throws Exception {
        // For file-based storage, we would need to rewrite the entire file
        // This is a simplified version that just logs what would be saved
        Misc.println("FileDatabase.saveGlobalObject: Would save object " + object.getObjectId() + 
                     " at " + object.getObjectX() + "," + object.getObjectY());
        // In a full implementation, you would read all objects, add/update this one, and rewrite the file
    }

    /**
     * Reload objects from config - for file database this just returns
     */
    public void loadObjectsFromConfig() throws Exception {
        // For file database, getAllGlobalObjects already loads from config
        Misc.println("FileDatabase.loadObjectsFromConfig: Objects are loaded directly from config file");
    }

    /**
     * Search global objects by object ID
     */
    public Objects[] searchObjectsById(int objectId) throws Exception {
        Objects[] allObjects = getAllGlobalObjects();
        List<Objects> matchingObjects = new ArrayList<>();
        
        for (Objects obj : allObjects) {
            if (obj != null && obj.getObjectId() == objectId) {
                matchingObjects.add(obj);
            }
        }
        
        System.out.println("Found " + matchingObjects.size() + " objects with ID " + objectId);
        return matchingObjects.toArray(new Objects[0]);
    }
}
