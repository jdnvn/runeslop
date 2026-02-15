package RS2.admin;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import RS2.GameEngine;
import RS2.model.player.PlayerHandler;
import RS2.model.npc.NPCHandler;
import RS2.model.npc.NPCList;
import RS2.model.item.ItemList;
import RS2.model.npc.NPC;
import RS2.admin.controllers.*;
import RS2.model.player.Client;
import RS2.model.object.Objects;
import RS2.db.DatabaseManager;

public class AdminPanel {
    
    private static HttpServer server;
    private static final int PORT = 4321;
    
    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Register endpoints
            server.createContext("/", new ControlPanelHandler());
            server.createContext("/api/players", new PlayersHandler());
            server.createContext("/api/kick", new KickHandler());
            server.createContext("/api/ban", new BanHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/command", new ServerCommandHandler());
            server.createContext("/api/players/send_message", new SendMessageHandler());
            
            // Agent API endpoints
            server.createContext("/agent/events", new AgentEventsHandler());
            server.createContext("/agent/npc_say", new AgentNpcSayHandler());
            server.createContext("/agent/get_npcs", new AgentGetNpcsHandler());
            server.createContext("/agent/walk_npc", new AgentWalkNpcHandler());
            server.createContext("/agent/get_npc_info", new AgentGetNpcInfoHandler());
            server.createContext("/agent/spawn_npc", new AgentSpawnNpcHandler());
            server.createContext("/agent/teleport_npc", new AgentTeleportNpcHandler());
            server.createContext("/agent/dialogue", new AgentDialogueHandler());
            server.createContext("/agent/give_item", new AgentGiveItemHandler());
            server.createContext("/agent/teleport", new AgentTeleportHandler());
            server.createContext("/agent/add_object", new AgentAddObjectHandler());
            server.createContext("/agent/remove_object", new AgentRemoveObjectHandler());
            server.createContext("/agent/get_objects", new AgentGetObjectsHandler());
            server.createContext("/agent/get_object_info", new AgentGetObjectInfoHandler());
            server.createContext("/agent/get_players", new AgentGetPlayersHandler());
            server.createContext("/agent/send_options", new AgentSendOptionsHandler());
            server.createContext("/agent/search_items_by_name", new AgentFindItemsByNameHandler());
            server.createContext("/agent/search_npcs_by_name", new AgentFindNpcsByNameHandler());
            
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println("Admin Panel started on http://localhost:" + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start Admin Panel: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Admin Panel stopped");
        }
    }
    
    static class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("{\"players\":[");
            int count = 0;
            for (int i = 0; i < PlayerHandler.players.length; i++) {
                if (PlayerHandler.players[i] != null) {
                    Client c = (Client) PlayerHandler.players[i];
                    if (count > 0) json.append(",");
                    json.append(String.format(
                        "{\"name\":\"%s\",\"rights\":\"%s\",\"combatLevel\":%d,\"x\":%d,\"y\":%d}",
                        c.playerName,
                        getRightsName(c.playerRights),
                        c.combatLevel,
                        c.absX,
                        c.absY
                    ));
                    count++;
                }
            }
            json.append("],\"totalRegistered\":").append(count).append("}");
            
            byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
    
    static class KickHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String playerName = query.split("=")[1];
            
            for (int i = 0; i < PlayerHandler.players.length; i++) {
                if (PlayerHandler.players[i] != null) {
                    Client c = (Client) PlayerHandler.players[i];
                    if (c.playerName.equalsIgnoreCase(playerName)) {
                        c.disconnected = true;
                        break;
                    }
                }
            }
            
            String response = "{\"success\":true}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    // TODO: doesn't work yet
    static class BanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"success\":true}";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String playerName = query.split("=")[1];
            
            Client player = null;
            for (int i = 0; i < PlayerHandler.players.length; i++) {
                if (PlayerHandler.players[i] != null) {
                    Client c = (Client) PlayerHandler.players[i];
                    if (c.playerName.equalsIgnoreCase(playerName)) {
                        player = c;
                        break;
                    }
                }
            }
            
            String json = "{}";
            if (player != null) {
                int totalLevel = 0;
                for (int i = 0; i < 23; i++) {
                    totalLevel += player.playerLevel[i];
                }
                
                json = String.format(
                    "{\"name\":\"%s\",\"combatLevel\":%d,\"totalLevel\":%d," +
                    "\"attack\":%d,\"strength\":%d,\"defence\":%d,\"hitpoints\":%d," +
                    "\"prayer\":%d,\"magic\":%d,\"ranged\":%d}",
                    player.playerName,
                    player.combatLevel,
                    totalLevel,
                    player.playerLevel[0],
                    player.playerLevel[2],
                    player.playerLevel[1],
                    player.playerLevel[3],
                    player.playerLevel[5],
                    player.playerLevel[6],
                    player.playerLevel[4]
                );
            }
            
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
    
    static class ServerCommandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String command = java.net.URLDecoder.decode(query.split("=")[1], "UTF-8");
            
            String result = executeServerCommand(command);
            String json = String.format("{\"success\":true,\"message\":\"%s\"}", result);
            
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
        
        private String executeServerCommand(String cmd) {
            try {
                String trimmed = cmd.trim();
                if (trimmed.isEmpty()) return "Empty command";
                
                String fullCommand = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
                return CommandHandler.executeServerCommand(fullCommand);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
    
    private static String getRightsName(int rights) {
        switch (rights) {
            case 0: return "Player";
            case 1: return "Moderator";
            case 2: return "Admin";
            case 3: return "Owner";
            default: return "Unknown";
        }
    }

    static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String message = extractString(body, "message");
            String playerName = extractString(body, "player_name");

            System.out.println("[DEBUG] Sending message to player: " + playerName);
            Client player = findPlayer(playerName);
            System.out.println("[DEBUG] Player found: " + (player != null));
            if (player == null) {
                sendJson(exchange, 404, "{\"error\":\"Player not found\"}");
                return;
            }

            player.sendMessage(message);
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // ==================== Agent API ====================
    
    // SSE connections for pushing events
    private static final List<OutputStream> sseClients = new CopyOnWriteArrayList<>();
    
    /**
     * Push an event to all connected SSE clients.
     * Call this from game code when events happen.
     */
    public static void pushAgentEvent(String eventType, String jsonData) {
        String sseMessage = "event: " + eventType + "\ndata: " + jsonData + "\n\n";
        byte[] bytes = sseMessage.getBytes(StandardCharsets.UTF_8);
        
        for (OutputStream os : sseClients) {
            try {
                os.write(bytes);
                os.flush();
            } catch (IOException e) {
                sseClients.remove(os);
            }
        }
    }
    
    /**
     * Called when player selects a dialogue option
     */
    public static void onDialogueOption(String playerName, int optionIndex, String optionText) {
        String json = "{\"player\":\"" + playerName + "\",\"option_index\":" + optionIndex + 
                      ",\"option_text\":\"" + optionText.replace("\"", "\\\"") + "\"}";
        pushAgentEvent("dialogue_option", json);
    }
    
    // SSE endpoint for game events
    static class AgentEventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.getResponseHeaders().set("Connection", "keep-alive");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, 0);
            
            OutputStream os = exchange.getResponseBody();
            sseClients.add(os);
            
            // Send initial connection message
            String welcome = "event: connected\ndata: {\"status\":\"connected\"}\n\n";
            os.write(welcome.getBytes(StandardCharsets.UTF_8));
            os.flush();
            
            // Keep connection open - it will be closed when client disconnects
            // The OutputStream stays in sseClients until write fails
            try {
                while (true) {
                    Thread.sleep(30000); // Keep-alive ping
                    os.write("event: ping\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            } catch (Exception e) {
                sseClients.remove(os);
            }
        }
    }
    
    // NPC forced chat
    static class AgentNpcSayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            int npcId = extractInt(body, "npc_id", -1);
            String npcName = extractString(body, "npc_name");
            String text = extractString(body, "text");
            
            if (npcId == -1 || npcName == null || text == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing npc_id, npc_name, or text\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                if (npcId >= 0 && npcId < NPCHandler.npcs.length && NPCHandler.npcs[npcId] != null) {
                    NPCHandler.npcs[npcId].forceChat(text);
                    for (int i = 0; i < PlayerHandler.players.length; i++) {
                        if (PlayerHandler.players[i] != null) {
                            Client c = (Client) PlayerHandler.players[i];
                            if (c.withinDistance(NPCHandler.npcs[npcId])) {
                                c.sendMessage(npcName + ": @blu@" + text);
                            }
                        }
                    }
                }
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // Spawn NPC
    static class AgentSpawnNpcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            int npcType = extractInt(body, "npc_type", -1);
            int x = extractInt(body, "x", -1);
            int y = extractInt(body, "y", -1);
            int height = extractInt(body, "height", 0);
            String customName = extractString(body, "name");
            int customCombatLevel = extractInt(body, "combat_level", -1);
            
            if (npcType == -1 || x == -1 || y == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing npc_type, x, or y\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                // Find a free slot
                int slot = -1;
                for (int i = 1; i < NPCHandler.maxNPCs; i++) {
                    if (NPCHandler.npcs[i] == null) {
                        slot = i;
                        break;
                    }
                }
                if (slot == -1) return;
                
                // Create the NPC
                NPC newNPC = new NPC(slot, npcType);
                newNPC.absX = x;
                newNPC.absY = y;
                newNPC.makeX = x;
                newNPC.makeY = y;
                newNPC.heightLevel = height;
                newNPC.walkingType = 1;
                newNPC.HP = 100;
                newNPC.MaxHP = 100;
                newNPC.maxHit = 1;
                newNPC.attack = 1;
                newNPC.defence = 1;
                
                // Set custom display name/combat if provided
                if (customName != null && !customName.isEmpty()) {
                    newNPC.customName = customName;
                    System.out.println("[DEBUG] Set NPC " + slot + " customName to: " + customName);
                }
                if (customCombatLevel >= 0) {
                    newNPC.customCombatLevel = customCombatLevel;
                    System.out.println("[DEBUG] Set NPC " + slot + " customCombatLevel to: " + customCombatLevel);
                }
                
                NPCHandler.npcs[slot] = newNPC;
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // Send dialogue to player
    static class AgentDialogueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            String player = extractString(body, "player");
            String npcName = extractString(body, "npc_name");
            String text = extractString(body, "text");
            int npcId = extractInt(body, "npc_id", 1);
            
            if (player == null || text == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing player or text\"}");
                return;
            }
            
            if (npcId < 0 || npcId > NPCHandler.npcs.length) npcId = 1;
            final int safeNpcId = npcId;
            
            GameEngine.pendingActions.add(() -> {
                Client c = findPlayer(player);
                if (c == null) return;
                NPC npc = NPCHandler.npcs[safeNpcId];
                if (npc == null) return;
                int npcTypeId = npc.npcType;
                if (c.withinDistance(npc))
                    npc.facePlayer(c.playerId);
                String[] lines = text.split("\\|");
                String name = npcName != null ? npcName : "NPC";
                
                switch (Math.min(lines.length, 4)) {
                    case 1:
                        c.getPA().sendFrame75(npcTypeId, 4883);
                        c.getPA().sendFrame200(4883, 591);
                        c.getPA().sendFrame126(name, 4884);
                        c.getPA().sendFrame126(lines[0], 4885);
                        c.getPA().sendFrame164(4882);
                        break;
                    case 2:
                        c.getPA().sendFrame75(npcTypeId, 4888);
                        c.getPA().sendFrame200(4888, 591);
                        c.getPA().sendFrame126(name, 4889);
                        c.getPA().sendFrame126(lines[0], 4890);
                        c.getPA().sendFrame126(lines[1], 4891);
                        c.getPA().sendFrame164(4887);
                        break;
                    case 3:
                        c.getPA().sendFrame75(npcTypeId, 4894);
                        c.getPA().sendFrame200(4894, 591);
                        c.getPA().sendFrame126(name, 4895);
                        c.getPA().sendFrame126(lines[0], 4896);
                        c.getPA().sendFrame126(lines[1], 4897);
                        c.getPA().sendFrame126(lines[2], 4898);
                        c.getPA().sendFrame164(4893);
                        break;
                    default:
                        c.getPA().sendFrame75(npcTypeId, 4901);
                        c.getPA().sendFrame200(4901, 591);
                        c.getPA().sendFrame126(name, 4902);
                        c.getPA().sendFrame126(lines[0], 4903);
                        c.getPA().sendFrame126(lines.length > 1 ? lines[1] : "", 4904);
                        c.getPA().sendFrame126(lines.length > 2 ? lines[2] : "", 4905);
                        c.getPA().sendFrame126(lines.length > 3 ? lines[3] : "", 4906);
                        c.getPA().sendFrame164(4900);
                }
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // Give item to player
    static class AgentGiveItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            String player = extractString(body, "player");
            int itemId = extractInt(body, "item_id", -1);
            int amount = extractInt(body, "amount", 1);
            
            if (player == null || itemId == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing player or item_id\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                Client c = findPlayer(player);
                if (c != null) {
                    c.getItems().addItem(itemId, amount);
                }
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // Teleport player
    static class AgentTeleportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            String player = extractString(body, "player");
            int x = extractInt(body, "x", -1);
            int y = extractInt(body, "y", -1);
            int height = extractInt(body, "height", 0);
            
            if (player == null || x == -1 || y == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing player, x, or y\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                Client c = findPlayer(player);
                if (c != null) {
                    c.getPA().movePlayer(x, y, height);
                }
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }
    
    // Get online players
    static class AgentGetPlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (int i = 0; i < PlayerHandler.players.length; i++) {
                if (PlayerHandler.players[i] != null) {
                    Client c = (Client) PlayerHandler.players[i];
                    if (count > 0) sb.append(",");
                    sb.append("{\"name\":\"").append(c.playerName)
                      .append("\",\"x\":").append(c.absX)
                      .append(",\"y\":").append(c.absY)
                      .append(",\"rights\":").append(c.playerRights).append("}");
                    count++;
                }
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"count\":" + count + ",\"players\":" + sb + "}");
        }
    }
    
    static class AgentGetNpcsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                StringBuilder sb = new StringBuilder("[");
                int count = 0;
                for (int i = 0; i < NPCHandler.npcs.length; i++) {
                    NPC npc = NPCHandler.npcs[i];
                    if (npc != null) {
                        if (count > 0) sb.append(",");
                        sb.append("{\"npc_id\":").append(i)
                          .append(",\"npc_type\":").append(npc.npcType)
                          .append(",\"x\":").append(npc.absX)
                          .append(",\"y\":").append(npc.absY).append("}");
                        count++;
                    }
                }
                sb.append("]");
                sendJson(exchange, 200, "{\"count\":" + count + ",\"npcs\":" + sb + "}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Move NPC
    static class AgentWalkNpcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!exchange.getRequestMethod().equals("POST")) {
                    sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }
                
                String body = readBody(exchange);
                int npcId = extractInt(body, "npc_id", -1);
                int x = extractInt(body, "x", -1);
                int y = extractInt(body, "y", -1);
                
                if (npcId == -1 || x == -1 || y == -1) {
                    sendJson(exchange, 400, "{\"error\":\"Missing npc_id, x, or y\"}");
                    return;
                }
                
                GameEngine.pendingActions.add(() -> {
                    NPC npc = NPCHandler.npcs[npcId];
                    if (npc == null) return;
                    npc.makeX = x;
                    npc.makeY = y;
                });
                
                sendJson(exchange, 200, "{\"success\":true}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Teleport NPC
    static class AgentTeleportNpcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            int npcId = extractInt(body, "npc_id", -1);
            int x = extractInt(body, "x", -1);
            int y = extractInt(body, "y", -1);

            if (npcId == -1 || x == -1 || y == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing npc_id, x, or y\"}");
                return;
            }

            GameEngine.pendingActions.add(() -> {
                NPC npc = NPCHandler.npcs[npcId];
                if (npc == null) return;
                npc.absX = x;
                npc.absY = y;
                npc.makeX = x;
                npc.makeY = y;
            });

            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    // Search NPCs by name
    static class AgentFindNpcsByNameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String name = extractString(body, "name");

            if (name == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing name\"}");
                return;
            }
            
            NPCList[] npcs = null;
            try {
                npcs = DatabaseManager.getInstance().searchNPCsByName(name);
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                return;
            }
            int count = 0;
            StringBuilder sb = new StringBuilder("[");
            for (NPCList npc : npcs) {
                if (count > 0) sb.append(",");
                sb.append("{\"npc_type_id\":" + npc.npcId + ",\"npc_name\":\"" + npc.npcName + "\",\"npc_combat\":" + npc.npcCombat + ",\"npc_health\":" + npc.npcHealth + "}");
                count++;
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"count\":" + count + ",\"npcs\":" + sb + "}");
        }
    }

    // Get NPC info
    static class AgentGetNpcInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            int npcId = extractInt(body, "npc_id", -1);
            
            if (npcId == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing npc_id\"}");
                return;
            }
            
            NPC npc = NPCHandler.npcs[npcId];
            if (npc == null) {
                sendJson(exchange, 404, "{\"error\":\"NPC not found\"}");
                return;
            }
            
            sendJson(exchange, 200, "{\"npc_id\":" + npcId + ",\"npc_type\":" + npc.npcType + ",\"x\":" + npc.absX + ",\"y\":" + npc.absY + ",\"hp\":" + npc.HP + ",\"max_hp\":" + npc.MaxHP + "}");
        }
    }

    // Send dialogue options to player
    static class AgentSendOptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            
            String body = readBody(exchange);
            String player = extractString(body, "player");
            String optionsJson = extractArray(body, "options");
            
            if (player == null || optionsJson == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing player or options\"}");
                return;
            }
            
            // Parse options array (simple parsing for ["opt1", "opt2", ...])
            String[] options = optionsJson.replace("[", "").replace("]", "")
                .replace("\"", "").split(",");
            for (int i = 0; i < options.length; i++) {
                options[i] = options[i].trim();
            }
            
            if (options.length < 2 || options.length > 5) {
                sendJson(exchange, 400, "{\"error\":\"Options must be 2-5 items\"}");
                return;
            }
            
            final String[] finalOptions = options;
            
            GameEngine.pendingActions.add(() -> {
                Client c = findPlayer(player);
                if (c == null) return;
                
                // Store options on client for when they click
                c.dialogueOptions = finalOptions;
                
                switch (finalOptions.length) {
                    case 2:
                        c.getPA().sendFrame126("Select an Option", 2460);
                        c.getPA().sendFrame126(finalOptions[0], 2461);
                        c.getPA().sendFrame126(finalOptions[1], 2462);
                        c.getPA().sendFrame164(2459);
                        break;
                    case 3:
                        c.getPA().sendFrame126("Select an Option", 2470);
                        c.getPA().sendFrame126(finalOptions[0], 2471);
                        c.getPA().sendFrame126(finalOptions[1], 2472);
                        c.getPA().sendFrame126(finalOptions[2], 2473);
                        c.getPA().sendFrame164(2469);
                        break;
                    case 4:
                        c.getPA().sendFrame126("Select an Option", 2481);
                        c.getPA().sendFrame126(finalOptions[0], 2482);
                        c.getPA().sendFrame126(finalOptions[1], 2483);
                        c.getPA().sendFrame126(finalOptions[2], 2484);
                        c.getPA().sendFrame126(finalOptions[3], 2485);
                        c.getPA().sendFrame164(2480);
                        break;
                    case 5:
                        c.getPA().sendFrame126("Select an Option", 2493);
                        c.getPA().sendFrame126(finalOptions[0], 2494);
                        c.getPA().sendFrame126(finalOptions[1], 2495);
                        c.getPA().sendFrame126(finalOptions[2], 2496);
                        c.getPA().sendFrame126(finalOptions[3], 2497);
                        c.getPA().sendFrame126(finalOptions[4], 2498);
                        c.getPA().sendFrame164(2492);
                        break;
                }
            });
            
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    // Add object
    static class AgentAddObjectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            int objectId = extractInt(body, "object_id", -1);
            int x = extractInt(body, "x", -1);
            int y = extractInt(body, "y", -1);
            int height = extractInt(body, "height", 0);
            int face = extractInt(body, "face", 0);
            int type = extractInt(body, "type", 10);

            if (objectId == -1 || x == -1 || y == -1 || height == -1 || face == -1 || type == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing object_id, x, y, height, face, or type\"}");
                return;
            }

            GameEngine.pendingActions.add(() -> {
                new RS2.model.object.Object(objectId, x, y, height, face, type, 0, 0, 0, 0, 0);
            });

            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    // Remove object
    static class AgentRemoveObjectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            int objectId = extractInt(body, "object_id", -1);
            
            if (objectId == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing object_id\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                Objects object = GameEngine.objectHandler.globalObjects.get(objectId);
                if (object == null) return;
                GameEngine.objectHandler.globalObjects.remove(object);
            });

            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    // Get object info
    static class AgentGetObjectInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            int objectId = extractInt(body, "object_id", -1);
            
            if (objectId == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing object_id\"}");
                return;
            }
            
            Objects object = GameEngine.objectHandler.globalObjects.get(objectId);
            if (object == null) {
                sendJson(exchange, 404, "{\"error\":\"Object not found\"}");
                return;
            }
            
            sendJson(exchange, 200, "{\"object_id\":" + objectId + ",\"object_type\":" + object.objectType + ",\"x\":" + object.getObjectX() + ",\"y\":" + object.getObjectY() + "}");
        }
    }

    // Get objects
    static class AgentGetObjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (int i = 0; i < GameEngine.objectHandler.globalObjects.size(); i++) {
                Objects object = GameEngine.objectHandler.globalObjects.get(i);
                if (count > 0) sb.append(",");
                sb.append("{\"object_id\":" + object.getObjectId() + ",\"object_type\":" + object.getObjectType() + ",\"x\":" + object.getObjectX() + ",\"y\":" + object.getObjectY() + "}");
                count++;
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"count\":" + count + ",\"objects\":" + sb + "}");
        }
    }

    // find item by name
    static class AgentFindItemsByNameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String name = extractString(body, "name");

            if (name == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing name\"}");
                return;
            }

            try {
                ItemList[] items = DatabaseManager.getInstance().searchItemsByName(name);
                if (items == null || items.length == 0) {
                    sendJson(exchange, 200, "{\"count\":0,\"items\":[]}");
                    return;
                }
                int count = 0;
                StringBuilder sb = new StringBuilder("[");
                for (ItemList item : items) {
                    if (item == null) continue;
                    if (count > 0) sb.append(",");
                    sb.append("{\"item_id\":" + item.itemId + 
                        ",\"item_name\":\"" + escapeJson(item.itemName) + "\"" +
                        ",\"item_description\":\"" + escapeJson(item.itemDescription) + "\"" +
                        ",\"shop_value\":" + item.ShopValue + "}");
                    count++;
                }
                sb.append("]");
                sendJson(exchange, 200, "{\"count\":" + count + ",\"items\":" + sb + "}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Helper methods for agent API
    private static Client findPlayer(String name) {
        for (int i = 0; i < PlayerHandler.players.length; i++) {
            if (PlayerHandler.players[i] != null) {
                Client c = (Client) PlayerHandler.players[i];
                if (c.playerName.equalsIgnoreCase(name)) return c;
            }
        }
        return null;
    }
    
    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
    
    private static String extractArray(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1) return null;
        int colon = json.indexOf(":", keyPos);
        if (colon == -1) return null;
        int start = json.indexOf("[", colon);
        if (start == -1) return null;
        int end = json.indexOf("]", start);
        if (end == -1) return null;
        return json.substring(start, end + 1);
    }
    
    private static String extractString(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1) return null;
        int colon = json.indexOf(":", keyPos);
        if (colon == -1) return null;
        int start = json.indexOf("\"", colon + 1);
        if (start == -1) return null;
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return null;
        return json.substring(start + 1, end);
    }
    
    private static int extractInt(String json, String key, int def) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1) return def;
        int colon = json.indexOf(":", keyPos);
        if (colon == -1) return def;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (start == end) return def;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (Exception e) { return def; }
    }
}

