package RS2.db;
import RS2.model.player.Player;
import RS2.model.npc.NPCList;
import RS2.model.npc.NPC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
                    "description TEXT," +
                    "PRIMARY KEY (npc_id)" +
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
                int npcId = rs.getInt("npc_id");
                int spawnX = rs.getInt("spawn_x");
                int spawnY = rs.getInt("spawn_y");
                int height = rs.getInt("height");
                int walk = rs.getInt("walk");
                int maxhit = rs.getInt("maxhit");
                int attack = rs.getInt("attack");
                int defence = rs.getInt("defence");
                String description = rs.getString("description");
                
                spawnedNPCs[index] = new NPC(npcId, spawnX, spawnY, height, walk, maxhit, attack, defence, description);
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
            preparedStatement.setInt(1, npc.npcId);
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
}
