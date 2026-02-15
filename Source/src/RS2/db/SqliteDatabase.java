package RS2.db;
import RS2.model.player.Player;
import RS2.model.npc.NPCList;
import RS2.model.npc.NPC;
import RS2.model.item.ItemList;
import RS2.model.object.Objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import RS2.Settings;
import RS2.util.Misc;

public class SqliteDatabase implements Database {

    private static final String DB_PATH = "./Data/game.db";
    private Connection conn;

    SqliteDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connect();
            createTables();
            loadNPCsFromConfig();
            loadItemsFromConfigInternal();
            loadObjectsFromConfigInternal();
            Misc.println("SQLite database initialized");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading SQLite JDBC driver");
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            System.out.println("Connected to SQLite database");
        } catch (SQLException e) {
            System.out.println("Error connecting to SQLite database");
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "height_level INTEGER DEFAULT 0," +
                    "pos_x INTEGER DEFAULT 3087," +
                    "pos_y INTEGER DEFAULT 3502," +
                    "rights INTEGER DEFAULT 0," +
                    "crystal_bow_arrows INTEGER DEFAULT 0," +
                    "skull_timer INTEGER DEFAULT 0," +
                    "magic_book INTEGER DEFAULT 0," +
                    "spec_amount REAL DEFAULT 10.0," +
                    "teleblock_delay INTEGER DEFAULT 0," +
                    "teleblock_length INTEGER DEFAULT 0," +
                    "auto_ret INTEGER DEFAULT 0," +
                    "flagged INTEGER DEFAULT 0" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_equipment (" +
                    "player_id INTEGER NOT NULL," +
                    "slot INTEGER NOT NULL," +
                    "item_id INTEGER," +
                    "amount INTEGER," +
                    "PRIMARY KEY (player_id, slot)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_appearance (" +
                    "player_id INTEGER NOT NULL," +
                    "look_0 INTEGER NOT NULL," +
                    "look_1 INTEGER NOT NULL," +
                    "look_2 INTEGER NOT NULL," +
                    "look_3 INTEGER NOT NULL," +
                    "look_4 INTEGER NOT NULL," +
                    "look_5 INTEGER NOT NULL," +
                    "look_6 INTEGER NOT NULL," +
                    "look_7 INTEGER NOT NULL," +
                    "look_8 INTEGER NOT NULL," +
                    "look_9 INTEGER NOT NULL," +
                    "look_10 INTEGER NOT NULL," +
                    "look_11 INTEGER NOT NULL," +
                    "look_12 INTEGER NOT NULL," +
                    "PRIMARY KEY (player_id)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                    "player_id INTEGER NOT NULL," +
                    "skill_id INTEGER NOT NULL," +
                    "level INTEGER," +
                    "xp INTEGER," +
                    "PRIMARY KEY (player_id, skill_id)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_inventory_items (" +
                    "player_id INTEGER NOT NULL," +
                    "slot INTEGER NOT NULL," +
                    "item_id INTEGER," +
                    "amount INTEGER," +
                    "PRIMARY KEY (player_id, slot)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_bank_items (" +
                    "player_id INTEGER NOT NULL," +
                    "slot INTEGER NOT NULL," +
                    "item_id INTEGER," +
                    "amount INTEGER," +
                    "PRIMARY KEY (player_id, slot)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_friends (" +
                    "player_id INTEGER NOT NULL," +
                    "friend_name INTEGER NOT NULL," +
                    "PRIMARY KEY (player_id, friend_name)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS player_ignores (" +
                    "player_id INTEGER NOT NULL," +
                    "ignore_name INTEGER NOT NULL," +
                    "PRIMARY KEY (player_id, ignore_name)," +
                    "FOREIGN KEY (player_id) REFERENCES players(id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS npcs (" +
                    "npc_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "combat INTEGER NOT NULL," +
                    "health INTEGER NOT NULL," +
                    "PRIMARY KEY (npc_id)" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS spawned_npcs (" +
                    "npc_id INTEGER NOT NULL," +
                    "spawn_x INTEGER NOT NULL," +
                    "spawn_y INTEGER NOT NULL," +
                    "height INTEGER NOT NULL," +
                    "walk INTEGER NOT NULL," +
                    "maxhit INTEGER NOT NULL," +
                    "attack INTEGER NOT NULL," +
                    "defence INTEGER NOT NULL," +
                    "description TEXT" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS items (" +
                    "item_id INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "shop_value REAL DEFAULT 0," +
                    "low_alch REAL DEFAULT 0," +
                    "high_alch REAL DEFAULT 0," +
                    "bonus_0 INTEGER DEFAULT 0," +
                    "bonus_1 INTEGER DEFAULT 0," +
                    "bonus_2 INTEGER DEFAULT 0," +
                    "bonus_3 INTEGER DEFAULT 0," +
                    "bonus_4 INTEGER DEFAULT 0," +
                    "bonus_5 INTEGER DEFAULT 0," +
                    "bonus_6 INTEGER DEFAULT 0," +
                    "bonus_7 INTEGER DEFAULT 0," +
                    "bonus_8 INTEGER DEFAULT 0," +
                    "bonus_9 INTEGER DEFAULT 0," +
                    "bonus_10 INTEGER DEFAULT 0," +
                    "bonus_11 INTEGER DEFAULT 0" +
                    ")";
            statement.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS global_objects (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "object_id INTEGER NOT NULL," +
                    "object_x INTEGER NOT NULL," +
                    "object_y INTEGER NOT NULL," +
                    "object_height INTEGER DEFAULT 0," +
                    "object_face INTEGER DEFAULT 0," +
                    "object_type INTEGER DEFAULT 10" +
                    ")";
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating tables");
            e.printStackTrace();
        }
    }

    private void loadNPCsFromConfig() {
        try (Statement statement = conn.createStatement()) {
            // Check if NPCs are already loaded
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM npcs");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("NPCs already loaded in database, skipping config import");
                rs.close();
                return;
            }
            rs.close();

            // Load NPCs from config file
            BufferedReader reader = new BufferedReader(new FileReader("./Data/cfg/npc.cfg"));
            String line;
            int count = 0;
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO npcs (npc_id, name, combat, health) VALUES (?, ?, ?, ?)"
            );
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                
                // Parse: npc = 0		Hans				0	0
                if (line.startsWith("npc = ")) {
                    String[] parts = line.substring(6).split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            int npcId = Integer.parseInt(parts[0]);
                            String name = parts[1].replace("_", " ");
                            int combat = Integer.parseInt(parts[2]);
                            int health = Integer.parseInt(parts[3]);
                            
                            insertStmt.setInt(1, npcId);
                            insertStmt.setString(2, name);
                            insertStmt.setInt(3, combat);
                            insertStmt.setInt(4, health);
                            insertStmt.executeUpdate();
                            count++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing NPC line: " + line);
                        }
                    }
                }
            }
            
            reader.close();
            insertStmt.close();
            System.out.println("Loaded " + count + " NPCs from config into database");
            
        } catch (SQLException | IOException e) {
            System.err.println("Error loading NPCs from config");
            e.printStackTrace();
        }
    }

    public PlayerRecord getPlayer(String playername) {
        ResultSet playerRecord = null;
        ResultSet equipment = null;
        ResultSet appearance = null;
        ResultSet skills = null;
        ResultSet inventory = null;
        ResultSet bank = null;
        ResultSet friends = null;
        ResultSet ignores = null;
        PlayerRecord player = new PlayerRecord(playername);
        try (Statement statement = conn.createStatement()) {
            playerRecord = statement.executeQuery("SELECT * FROM players WHERE username = '" + playername + "' LIMIT 1");
            if (!playerRecord.next()) {
                player.newPlayer = false;
                playerRecord.close();
                return player;
            }
            player.exists = true;
            player.id = playerRecord.getInt("id");
            player.username = playerRecord.getString("username");
            player.password = playerRecord.getString("password");
            player.heightLevel = playerRecord.getInt("height_level");
            player.teleportToX = playerRecord.getInt("pos_x");
            player.teleportToY = playerRecord.getInt("pos_y");
            player.playerRights = playerRecord.getInt("rights");
            player.crystalBowArrowCount = playerRecord.getInt("crystal_bow_arrows");
            player.skullTimer = playerRecord.getInt("skull_timer");
            player.playerMagicBook = playerRecord.getInt("magic_book");
            player.specAmount = playerRecord.getDouble("spec_amount");
            player.teleBlockDelay = playerRecord.getLong("teleblock_delay");
            player.teleBlockLength = playerRecord.getInt("teleblock_length");
            player.autoRet = playerRecord.getInt("auto_ret");
            player.accountFlagged = playerRecord.getBoolean("flagged");
            playerRecord.close();

            equipment = statement.executeQuery("SELECT * FROM player_equipment WHERE player_id = '" + player.id + "'");

            while (equipment.next()) {
                player.playerEquipment[equipment.getInt("slot")] = equipment.getInt("item_id");
                player.playerEquipmentN[equipment.getInt("slot")] = equipment.getInt("amount");
            }

            equipment.close();

            appearance = statement.executeQuery("SELECT * FROM player_appearance WHERE player_id = '" + player.id + "'" + " LIMIT 1");
            while (appearance.next()) {
                for (int i = 0; i < 13; i++) {
                    player.playerAppearance[i] = appearance.getInt("look_" + i);
                }
            }
            appearance.close();

            skills = statement.executeQuery("SELECT * FROM player_skills WHERE player_id = '" + player.id + "'");
            while (skills.next()) {
                player.playerLevel[skills.getInt("skill_id")] = skills.getInt("level");
                player.playerXP[skills.getInt("skill_id")] = skills.getInt("xp");
            }
            skills.close();

            inventory = statement.executeQuery("SELECT * FROM player_inventory_items WHERE player_id = '" + player.id + "'");
            while (inventory.next()) {
                player.playerItems[inventory.getInt("slot")] = inventory.getInt("item_id");
                player.playerItemsN[inventory.getInt("slot")] = inventory.getInt("amount");
            }
            System.out.println("Inventory: " + Arrays.toString(player.playerItems));
            inventory.close();

            bank = statement.executeQuery("SELECT * FROM player_bank_items WHERE player_id = '" + player.id + "'");
            while (bank.next()) {
                player.bankItems[bank.getInt("slot")] = bank.getInt("item_id");
                player.bankItemsN[bank.getInt("slot")] = bank.getInt("amount");
            }
            bank.close();
    
            friends = statement.executeQuery("SELECT * FROM player_friends WHERE player_id = '" + player.id + "'");
            int i = 0;
            while (friends.next()) {
                player.friends[i] = friends.getLong("friend_name");
                i++;
            }

            ignores = statement.executeQuery("SELECT * FROM player_ignores WHERE player_id = '" + player.id + "'");
            i = 0;
            while (ignores.next()) {
                player.ignores[i] = ignores.getLong("ignore_name");
                i++;
            }
            ignores.close();
        } catch (SQLException e) {
            System.out.println("Error querying player");
            e.printStackTrace();
            player.loadError = true;
            return player;
        }

        return player;
    }

    public boolean savePlayer(Player player) {
        try (Statement statement = conn.createStatement()) {
            System.out.println("Saving player: " + player.playerName);
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO players (username, password, height_level, pos_x, pos_y, rights, crystal_bow_arrows, skull_timer, magic_book, spec_amount, teleblock_delay, teleblock_length, auto_ret, flagged) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(username) DO UPDATE SET height_level = ?, pos_x = ?, pos_y = ?, rights = ?, crystal_bow_arrows = ?, skull_timer = ?, magic_book = ?, spec_amount = ?, teleblock_delay = ?, teleblock_length = ?, auto_ret = ?, flagged = ?");

            preparedStatement.setString(1, player.playerName);
            preparedStatement.setString(2, player.playerPass);
            preparedStatement.setInt(3, player.heightLevel);
            preparedStatement.setInt(4, player.absX);
            preparedStatement.setInt(5, player.absY);
            preparedStatement.setInt(6, player.playerRights);
            preparedStatement.setInt(7, player.crystalBowArrowCount);
            preparedStatement.setInt(8, player.skullTimer);
            preparedStatement.setInt(9, player.playerMagicBook);
            preparedStatement.setDouble(10, player.specAmount);
            preparedStatement.setLong(11, player.teleBlockDelay);
            preparedStatement.setInt(12, player.teleBlockLength);
            preparedStatement.setInt(13, player.autoRet);
            preparedStatement.setBoolean(14, player.accountFlagged);

            preparedStatement.setInt(15, player.heightLevel);
            preparedStatement.setInt(16, player.absX);
            preparedStatement.setInt(17, player.absY);
            preparedStatement.setInt(18, player.playerRights);
            preparedStatement.setInt(19, player.crystalBowArrowCount);
            preparedStatement.setInt(20, player.skullTimer);
            preparedStatement.setInt(21, player.playerMagicBook);
            preparedStatement.setDouble(22, player.specAmount);
            preparedStatement.setLong(23, player.teleBlockDelay);
            preparedStatement.setInt(24, player.teleBlockLength);
            preparedStatement.setInt(25, player.autoRet);
            preparedStatement.setBoolean(26, player.accountFlagged);

            preparedStatement.executeUpdate();
            preparedStatement.close();

            if (player.playerEquipment.length > 0) {
                String sqlEquipment = "INSERT INTO player_equipment (player_id, slot, item_id, amount) VALUES ";
                for (int i = 0; i < player.playerEquipment.length; i++) {
                    sqlEquipment += "(?, ?, ?, ?)";
                    if (i < player.playerEquipment.length - 1) {
                        sqlEquipment += ", ";
                    }
                }
                sqlEquipment += " ON CONFLICT(player_id, slot) DO UPDATE SET item_id = excluded.item_id, amount = excluded.amount";
                PreparedStatement preparedStatementEquipment = conn.prepareStatement(sqlEquipment);

                for (int i = 0; i < player.playerEquipment.length; i++) {
                    preparedStatementEquipment.setInt(1 + (i * 4), player.playerRecordId);
                    preparedStatementEquipment.setInt(2 + (i * 4), i);
                    preparedStatementEquipment.setInt(3 + (i * 4), player.playerEquipment[i]);
                    preparedStatementEquipment.setInt(4 + (i * 4), player.playerEquipmentN[i]);
                }
                preparedStatementEquipment.executeUpdate();
                preparedStatementEquipment.close();
            }

            if (player.playerAppearance.length > 0) {
                String sqlAppearance = "INSERT INTO player_appearance (player_id, look_0, look_1, look_2, look_3, look_4, look_5, look_6, look_7, look_8, look_9, look_10, look_11, look_12) VALUES ";
                sqlAppearance += "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(player_id) DO UPDATE SET look_0 = ?, look_1 = ?, look_2 = ?, look_3 = ?, look_4 = ?, look_5 = ?, look_6 = ?, look_7 = ?, look_8 = ?, look_9 = ?, look_10 = ?, look_11 = ?, look_12 = ?";
                PreparedStatement preparedStatementAppearance = conn.prepareStatement(sqlAppearance);
                preparedStatementAppearance.setInt(1, player.playerRecordId);
                preparedStatementAppearance.setInt(2, player.playerAppearance[0]);
                preparedStatementAppearance.setInt(3, player.playerAppearance[1]);
                preparedStatementAppearance.setInt(4, player.playerAppearance[2]);
                preparedStatementAppearance.setInt(5, player.playerAppearance[3]);
                preparedStatementAppearance.setInt(6, player.playerAppearance[4]);
                preparedStatementAppearance.setInt(7, player.playerAppearance[5]);
                preparedStatementAppearance.setInt(8, player.playerAppearance[6]);
                preparedStatementAppearance.setInt(9, player.playerAppearance[7]);
                preparedStatementAppearance.setInt(10, player.playerAppearance[8]);
                preparedStatementAppearance.setInt(11, player.playerAppearance[9]);
                preparedStatementAppearance.setInt(12, player.playerAppearance[10]);
                preparedStatementAppearance.setInt(13, player.playerAppearance[11]);
                preparedStatementAppearance.setInt(14, player.playerAppearance[12]);

                preparedStatementAppearance.setInt(15, player.playerAppearance[0]);
                preparedStatementAppearance.setInt(16, player.playerAppearance[1]);
                preparedStatementAppearance.setInt(17, player.playerAppearance[2]);
                preparedStatementAppearance.setInt(18, player.playerAppearance[3]);
                preparedStatementAppearance.setInt(19, player.playerAppearance[4]);
                preparedStatementAppearance.setInt(20, player.playerAppearance[5]);
                preparedStatementAppearance.setInt(21, player.playerAppearance[6]);
                preparedStatementAppearance.setInt(22, player.playerAppearance[7]);
                preparedStatementAppearance.setInt(23, player.playerAppearance[8]);
                preparedStatementAppearance.setInt(24, player.playerAppearance[9]);
                preparedStatementAppearance.setInt(25, player.playerAppearance[10]);
                preparedStatementAppearance.setInt(26, player.playerAppearance[11]);
                preparedStatementAppearance.setInt(27, player.playerAppearance[12]);
                preparedStatementAppearance.executeUpdate();
                preparedStatementAppearance.close();
            }

            if (player.playerLevel.length > 0) {
                String sqlSkills = "INSERT INTO player_skills (player_id, skill_id, level, xp) VALUES ";
                for (int i = 0; i < player.playerLevel.length; i++) {
                    sqlSkills += "(?, ?, ?, ?)";
                    if (i < player.playerLevel.length - 1) {
                        sqlSkills += ", ";
                    }
                }

                sqlSkills += " ON CONFLICT(player_id, skill_id) DO UPDATE SET level = excluded.level, xp = excluded.xp";
                PreparedStatement preparedStatementSkills = conn.prepareStatement(sqlSkills);

                for (int i = 0; i < player.playerLevel.length; i++) {
                    preparedStatementSkills.setInt(1 + (i * 4), player.playerRecordId);
                    preparedStatementSkills.setInt(2 + (i * 4), i);
                    preparedStatementSkills.setInt(3 + (i * 4), player.playerLevel[i]);
                    preparedStatementSkills.setInt(4 + (i * 4), player.playerXP[i]);
                }
                preparedStatementSkills.executeUpdate();
                preparedStatementSkills.close();
            }

            if (player.playerItems.length > 0) {
                String sqlInventory = "INSERT INTO player_inventory_items (player_id, slot, item_id, amount) VALUES ";
                for (int i = 0; i < player.playerItems.length; i++) {
                    sqlInventory += "(?, ?, ?, ?)";
                    if (i < player.playerItems.length - 1) {
                        sqlInventory += ", ";
                    }
                }

                sqlInventory += " ON CONFLICT(player_id, slot) DO UPDATE SET item_id = excluded.item_id, amount = excluded.amount";
                PreparedStatement preparedStatementInventory = conn.prepareStatement(sqlInventory);

                for (int i = 0; i < player.playerItems.length; i++) {
                    preparedStatementInventory.setInt(1 + (i * 4), player.playerRecordId);
                    preparedStatementInventory.setInt(2 + (i * 4), i);
                    preparedStatementInventory.setInt(3 + (i * 4), player.playerItems[i]);
                    preparedStatementInventory.setInt(4 + (i * 4), player.playerItemsN[i]);
                }
                preparedStatementInventory.executeUpdate();
                preparedStatementInventory.close();
            }

            if (player.bankItems.length > 0) {
                String sqlBank = "INSERT INTO player_bank_items (player_id, slot, item_id, amount) VALUES ";
                for (int i = 0; i < player.bankItems.length; i++) {
                    sqlBank += "(?, ?, ?, ?)";
                    if (i < player.bankItems.length - 1) {
                        sqlBank += ", ";
                    }
                }

                sqlBank += " ON CONFLICT(player_id, slot) DO UPDATE SET item_id = excluded.item_id, amount = excluded.amount";
                PreparedStatement preparedStatementBank = conn.prepareStatement(sqlBank);

                for (int i = 0; i < player.bankItems.length; i++) {
                    preparedStatementBank.setInt(1 + (i * 4), player.playerRecordId);
                    preparedStatementBank.setInt(2 + (i * 4), i);
                    preparedStatementBank.setInt(3 + (i * 4), player.bankItems[i]);
                    preparedStatementBank.setInt(4 + (i * 4), player.bankItemsN[i]);
                }
                preparedStatementBank.executeUpdate();
                preparedStatementBank.close();
            }

            if (player.friends.length > 0) {
                String sqlFriends = "INSERT INTO player_friends (player_id, friend_name) VALUES ";
                for (int i = 0; i < player.friends.length; i++) {
                    sqlFriends += "(?, ?)";
                    if (i < player.friends.length - 1) {
                        sqlFriends += ", ";
                    }
                }

                sqlFriends += " ON CONFLICT(player_id, friend_name) DO NOTHING";
                PreparedStatement preparedStatementFriends = conn.prepareStatement(sqlFriends);

                for (int i = 0; i < player.friends.length; i++) {
                    preparedStatementFriends.setInt(1 + (i * 2), player.playerRecordId);
                    preparedStatementFriends.setLong(2 + (i * 2), player.friends[i]);
                }
                preparedStatementFriends.executeUpdate();
                preparedStatementFriends.close();
            }

            if (player.ignores.length > 0) {
                String sqlIgnores = "INSERT INTO player_ignores (player_id, ignore_name) VALUES ";
                for (int i = 0; i < player.ignores.length; i++) {
                    sqlIgnores += "(?, ?)";
                    if (i < player.ignores.length - 1) {
                        sqlIgnores += ", ";
                    }
                }

                sqlIgnores += " ON CONFLICT(player_id, ignore_name) DO NOTHING";
                PreparedStatement preparedStatementIgnores = conn.prepareStatement(sqlIgnores);

                for (int i = 0; i < player.ignores.length; i++) {
                    preparedStatementIgnores.setInt(1 + (i * 2), player.playerRecordId);
                    preparedStatementIgnores.setLong(2 + (i * 2), player.ignores[i]);
                }
                preparedStatementIgnores.executeUpdate();
                preparedStatementIgnores.close();
            }

            System.out.println("Player saved: " + player.playerName);
        } catch (SQLException e) {
            System.out.println("Error saving player");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public NPCList[] getAllNPCs() throws Exception {
        NPCList[] npcs = new NPCList[Settings.MAX_LISTED_NPCS];
        int index = 0;
        
        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT npc_id, name, combat, health FROM npcs ORDER BY npc_id");
            
            while (rs.next() && index < npcs.length) {
                int npcId = rs.getInt("npc_id");
                String name = rs.getString("name");
                int combat = rs.getInt("combat");
                int health = rs.getInt("health");
                
                npcs[index] = new NPCList(npcId, name, combat, health);
                index++;
            }
            
            rs.close();
            System.out.println("Loaded " + index + " NPCs from database");
        } catch (SQLException e) {
            System.err.println("Error loading NPCs from database");
            e.printStackTrace();
            throw e;
        }
        
        return npcs;
    }

    public NPC[] getAllSpawnedNPCs() throws Exception {
        NPC[] spawnedNPCs = new NPC[Settings.MAX_NPCS];
        int index = 0;
        
        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT npc_id, spawn_x, spawn_y, height, walk, maxhit, attack, defence, description FROM spawned_npcs ORDER BY npc_id");
            
            while (rs.next() && index < spawnedNPCs.length) {
                int npcType = rs.getInt("npc_id");
                int spawnX = rs.getInt("spawn_x");
                int spawnY = rs.getInt("spawn_y");
                int height = rs.getInt("height");
                int walk = rs.getInt("walk");
                int maxhit = rs.getInt("maxhit");
                int attack = rs.getInt("attack");
                int defence = rs.getInt("defence");
                String description = rs.getString("description");
                
                spawnedNPCs[index] = new NPC(npcType, spawnX, spawnY, height, walk, maxhit, attack, defence, description);
                index++;
            }
            
            rs.close();
            System.out.println("Loaded " + index + " spawned NPCs from database");
        } catch (SQLException e) {
            System.err.println("Error loading spawned NPCs from database");
            e.printStackTrace();
            throw e;
        }
        
        return spawnedNPCs;
    }

    public void saveSpawnedNPC(NPC npc) throws Exception {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO spawned_npcs (npc_id, spawn_x, spawn_y, height, walk, maxhit, attack, defence, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, npc.npcType);
            preparedStatement.setInt(2, npc.spawnX);
            preparedStatement.setInt(3, npc.spawnY);
            preparedStatement.setInt(4, npc.heightLevel);
            preparedStatement.setInt(5, npc.walkingType);
            preparedStatement.setInt(6, npc.maxHit);
            preparedStatement.setInt(7, npc.attack);
            preparedStatement.setInt(8, npc.defence);
            preparedStatement.setString(9, npc.description);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error saving spawned NPC");
            e.printStackTrace();
            throw e;
        }
    }

    public NPCList[] searchNPCsByName(String name) throws Exception {
        List<NPCList> npcList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT npc_id, name, combat, health FROM npcs WHERE LOWER(name) LIKE LOWER(?)");
            preparedStatement.setString(1, "%" + name + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int npcId = rs.getInt("npc_id");
                String npcName = rs.getString("name");
                int npcCombat = rs.getInt("combat");
                int npcHealth = rs.getInt("health");
                npcList.add(new NPCList(npcId, npcName, npcCombat, npcHealth));
            }
            rs.close();
            preparedStatement.close();
            System.out.println("Found " + npcList.size() + " NPCs matching '" + name + "'");
        } catch (SQLException e) {
            System.err.println("Error searching NPCs by name: " + name);
            e.printStackTrace();
            throw e;
        }
        return npcList.toArray(new NPCList[0]);
    }

    // ==================== ITEM METHODS ====================

    /**
     * Internal method to load items from config file into database (called on startup)
     */
    private void loadItemsFromConfigInternal() {
        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM items");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Items already loaded in database, skipping config import");
                rs.close();
                return;
            }
            rs.close();

            BufferedReader reader = new BufferedReader(new FileReader("./Data/cfg/item.cfg"));
            String line;
            int count = 0;
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO items (item_id, name, description, shop_value, low_alch, high_alch, " +
                "bonus_0, bonus_1, bonus_2, bonus_3, bonus_4, bonus_5, bonus_6, bonus_7, bonus_8, bonus_9, bonus_10, bonus_11) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//") || line.equals("[ENDOFITEMLIST]")) {
                    continue;
                }
                
                // Parse: item = ID Name Description ShopValue LowAlch HighAlch Bonuses[0-11]
                if (line.startsWith("item = ")) {
                    String data = line.substring(7);
                    data = data.replaceAll("\t\t+", "\t");
                    String[] parts = data.split("\t");
                    
                    if (parts.length >= 6) {
                        try {
                            int itemId = Integer.parseInt(parts[0]);
                            String name = parts[1].replace("_", " ");
                            String description = parts[2].replace("_", " ");
                            double shopValue = Double.parseDouble(parts[3]);
                            double lowAlch = Double.parseDouble(parts[4]);
                            double highAlch = Double.parseDouble(parts[5]);
                            
                            int[] bonuses = new int[12];
                            for (int i = 0; i < 12 && (6 + i) < parts.length; i++) {
                                try {
                                    bonuses[i] = Integer.parseInt(parts[6 + i]);
                                } catch (NumberFormatException e) {
                                    bonuses[i] = 0;
                                }
                            }
                            
                            insertStmt.setInt(1, itemId);
                            insertStmt.setString(2, name);
                            insertStmt.setString(3, description);
                            insertStmt.setDouble(4, shopValue);
                            insertStmt.setDouble(5, lowAlch);
                            insertStmt.setDouble(6, highAlch);
                            for (int i = 0; i < 12; i++) {
                                insertStmt.setInt(7 + i, bonuses[i]);
                            }
                            insertStmt.executeUpdate();
                            count++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing item line: " + line);
                        }
                    }
                }
            }
            
            reader.close();
            insertStmt.close();
            System.out.println("Loaded " + count + " items from config into database");
            
        } catch (SQLException | IOException e) {
            System.err.println("Error loading items from config");
            e.printStackTrace();
        }
    }

    /**
     * Public method to force reload items from config (clears existing items first)
     */
    public void loadItemsFromConfig() throws Exception {
        try (Statement statement = conn.createStatement()) {
            statement.execute("DELETE FROM items");
            System.out.println("Cleared items table");
        }
        loadItemsFromConfigInternal();
    }

    /**
     * Get all items from database
     */
    public ItemList[] getAllItems() throws Exception {
        List<ItemList> itemsList = new ArrayList<>();
        
        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery(
                "SELECT item_id, name, description, shop_value, low_alch, high_alch, " +
                "bonus_0, bonus_1, bonus_2, bonus_3, bonus_4, bonus_5, bonus_6, bonus_7, bonus_8, bonus_9, bonus_10, bonus_11 " +
                "FROM items ORDER BY item_id"
            );
            
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                ItemList item = new ItemList(itemId);
                item.itemName = rs.getString("name");
                item.itemDescription = rs.getString("description");
                item.ShopValue = rs.getDouble("shop_value");
                item.LowAlch = rs.getDouble("low_alch");
                item.HighAlch = rs.getDouble("high_alch");
                
                for (int i = 0; i < 12; i++) {
                    item.Bonuses[i] = rs.getInt("bonus_" + i);
                }
                
                itemsList.add(item);
            }
            
            rs.close();
            System.out.println("Loaded " + itemsList.size() + " items from database");
        } catch (SQLException e) {
            System.err.println("Error loading items from database");
            e.printStackTrace();
            throw e;
        }
        
        return itemsList.toArray(new ItemList[0]);
    }

    /**
     * Save a single item to database
     */
    public void saveItem(ItemList item) throws Exception {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                "INSERT OR REPLACE INTO items (item_id, name, description, shop_value, low_alch, high_alch, " +
                "bonus_0, bonus_1, bonus_2, bonus_3, bonus_4, bonus_5, bonus_6, bonus_7, bonus_8, bonus_9, bonus_10, bonus_11) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            preparedStatement.setInt(1, item.itemId);
            preparedStatement.setString(2, item.itemName);
            preparedStatement.setString(3, item.itemDescription);
            preparedStatement.setDouble(4, item.ShopValue);
            preparedStatement.setDouble(5, item.LowAlch);
            preparedStatement.setDouble(6, item.HighAlch);
            for (int i = 0; i < 12; i++) {
                preparedStatement.setInt(7 + i, item.Bonuses[i]);
            }
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error saving item: " + item.itemId);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Search items by name (case-insensitive, partial match)
     */
    public ItemList[] searchItemsByName(String name) throws Exception {
        List<ItemList> itemsList = new ArrayList<>();
        
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                "SELECT item_id, name, description, shop_value, low_alch, high_alch, " +
                "bonus_0, bonus_1, bonus_2, bonus_3, bonus_4, bonus_5, bonus_6, bonus_7, bonus_8, bonus_9, bonus_10, bonus_11 " +
                "FROM items WHERE LOWER(name) LIKE LOWER(?) ORDER BY item_id"
            );
            preparedStatement.setString(1, "%" + name + "%");
            ResultSet rs = preparedStatement.executeQuery();
            
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                ItemList item = new ItemList(itemId);
                item.itemName = rs.getString("name");
                item.itemDescription = rs.getString("description");
                item.ShopValue = rs.getDouble("shop_value");
                item.LowAlch = rs.getDouble("low_alch");
                item.HighAlch = rs.getDouble("high_alch");
                
                for (int i = 0; i < 12; i++) {
                    item.Bonuses[i] = rs.getInt("bonus_" + i);
                }
                
                itemsList.add(item);
            }
            
            rs.close();
            preparedStatement.close();
            System.out.println("Found " + itemsList.size() + " items matching '" + name + "'");
        } catch (SQLException e) {
            System.err.println("Error searching items by name: " + name);
            e.printStackTrace();
            throw e;
        }
        
        return itemsList.toArray(new ItemList[0]);
    }

    // ==================== OBJECT METHODS ====================

    /**
     * Internal method to load global objects from config file into database (called on startup)
     */
    private void loadObjectsFromConfigInternal() {
        try (Statement statement = conn.createStatement()) {
            // Check if objects are already loaded
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM global_objects");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Global objects already loaded in database, skipping config import");
                rs.close();
                return;
            }
            rs.close();

            // Load objects from config file
            BufferedReader reader = new BufferedReader(new FileReader("./Data/cfg/global-objects.cfg"));
            String line;
            int count = 0;
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO global_objects (object_id, object_x, object_y, object_height, object_face, object_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//") || line.equals("[ENDOFOBJECTLIST]")) {
                    continue;
                }
                
                // Parse: object = objectId X Y Height Face objectType
                if (line.startsWith("object = ")) {
                    String data = line.substring(9);
                    // Replace multiple tabs with single tab
                    data = data.replaceAll("\t\t+", "\t");
                    String[] parts = data.split("\t");
                    
                    if (parts.length >= 6) {
                        try {
                            int objectId = Integer.parseInt(parts[0]);
                            int objectX = Integer.parseInt(parts[1]);
                            int objectY = Integer.parseInt(parts[2]);
                            int objectHeight = Integer.parseInt(parts[3]);
                            int objectFace = Integer.parseInt(parts[4]);
                            int objectType = Integer.parseInt(parts[5]);
                            
                            insertStmt.setInt(1, objectId);
                            insertStmt.setInt(2, objectX);
                            insertStmt.setInt(3, objectY);
                            insertStmt.setInt(4, objectHeight);
                            insertStmt.setInt(5, objectFace);
                            insertStmt.setInt(6, objectType);
                            insertStmt.executeUpdate();
                            count++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing object line: " + line);
                        }
                    }
                }
            }
            
            reader.close();
            insertStmt.close();
            System.out.println("Loaded " + count + " global objects from config into database");
            
        } catch (SQLException | IOException e) {
            System.err.println("Error loading global objects from config");
            e.printStackTrace();
        }
    }

    /**
     * Public method to force reload objects from config (clears existing objects first)
     */
    public void loadObjectsFromConfig() throws Exception {
        try (Statement statement = conn.createStatement()) {
            statement.execute("DELETE FROM global_objects");
            System.out.println("Cleared global_objects table");
        }
        loadObjectsFromConfigInternal();
    }

    /**
     * Get all global objects from database
     */
    public Objects[] getAllGlobalObjects() throws Exception {
        List<Objects> objectsList = new ArrayList<>();
        
        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery(
                "SELECT object_id, object_x, object_y, object_height, object_face, object_type " +
                "FROM global_objects ORDER BY id"
            );
            
            while (rs.next()) {
                int objectId = rs.getInt("object_id");
                int objectX = rs.getInt("object_x");
                int objectY = rs.getInt("object_y");
                int objectHeight = rs.getInt("object_height");
                int objectFace = rs.getInt("object_face");
                int objectType = rs.getInt("object_type");
                
                Objects obj = new Objects(objectId, objectX, objectY, objectHeight, objectFace, objectType, 0);
                objectsList.add(obj);
            }
            
            rs.close();
            System.out.println("Loaded " + objectsList.size() + " global objects from database");
        } catch (SQLException e) {
            System.err.println("Error loading global objects from database");
            e.printStackTrace();
            throw e;
        }
        
        return objectsList.toArray(new Objects[0]);
    }

    /**
     * Save a single global object to database
     */
    public void saveGlobalObject(Objects object) throws Exception {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                "INSERT INTO global_objects (object_id, object_x, object_y, object_height, object_face, object_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            preparedStatement.setInt(1, object.getObjectId());
            preparedStatement.setInt(2, object.getObjectX());
            preparedStatement.setInt(3, object.getObjectY());
            preparedStatement.setInt(4, object.getObjectHeight());
            preparedStatement.setInt(5, object.getObjectFace());
            preparedStatement.setInt(6, object.getObjectType());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error saving global object: " + object.getObjectId());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Search global objects by object ID
     */
    public Objects[] searchObjectsById(int objectId) throws Exception {
        List<Objects> objectsList = new ArrayList<>();
        
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                "SELECT object_id, object_x, object_y, object_height, object_face, object_type " +
                "FROM global_objects WHERE object_id = ? ORDER BY id"
            );
            preparedStatement.setInt(1, objectId);
            ResultSet rs = preparedStatement.executeQuery();
            
            while (rs.next()) {
                int objId = rs.getInt("object_id");
                int objectX = rs.getInt("object_x");
                int objectY = rs.getInt("object_y");
                int objectHeight = rs.getInt("object_height");
                int objectFace = rs.getInt("object_face");
                int objectType = rs.getInt("object_type");
                
                Objects obj = new Objects(objId, objectX, objectY, objectHeight, objectFace, objectType, 0);
                objectsList.add(obj);
            }
            
            rs.close();
            preparedStatement.close();
            System.out.println("Found " + objectsList.size() + " objects with ID " + objectId);
        } catch (SQLException e) {
            System.err.println("Error searching objects by ID: " + objectId);
            e.printStackTrace();
            throw e;
        }

        return objectsList.toArray(new Objects[0]);
    }
}
