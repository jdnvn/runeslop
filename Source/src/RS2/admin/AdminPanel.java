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
import RS2.model.npc.NPC;
import RS2.Settings;
import RS2.model.player.Client;
import RS2.model.object.Objects;

public class AdminPanel {
    
    private static HttpServer server;
    private static final int PORT = 4321;
    
    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Register endpoints
            server.createContext("/", new DashboardHandler());
            server.createContext("/api/players", new PlayersHandler());
            server.createContext("/api/kick", new KickHandler());
            server.createContext("/api/ban", new BanHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/command", new ServerCommandHandler());
            
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
    
    // Dashboard HTML
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getDashboardHTML();
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
        
        private String getDashboardHTML() {
            return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>" + Settings.SERVER_NAME + " Admin Panel</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { font-family: 'Courier New', monospace; background: #2d2416; color: #ffff00; }\n" +
                "        .header { background: linear-gradient(180deg, #584123 0%, #3d2c18 100%); padding: 15px; border-bottom: 2px solid #8b7355; box-shadow: inset 0 0 30px rgba(0,0,0,0.5); }\n" +
                "        .header h1 { color: #ffff00; font-size: 24px; text-shadow: 2px 2px 4px #000; text-align: center; }\n" +
                "        .container { padding: 20px; max-width: 1400px; margin: 0 auto; display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }\n" +
                "        .panel { background: linear-gradient(180deg, #584123 0%, #3d2c18 100%); border: 3px solid #8b7355; border-radius: 5px; padding: 15px; box-shadow: 0 4px 10px rgba(0,0,0,0.5), inset 0 0 20px rgba(0,0,0,0.3); }\n" +
                "        .panel-title { color: #ff9800; font-size: 16px; font-weight: bold; margin-bottom: 10px; text-shadow: 1px 1px 2px #000; border-bottom: 2px solid #8b7355; padding-bottom: 5px; }\n" +
                "        .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 15px; }\n" +
                "        .stat-card { background: #1a1410; padding: 15px; border: 2px solid #5a4a35; border-radius: 3px; text-align: center; }\n" +
                "        .stat-card .label { color: #ff9800; font-size: 11px; margin-bottom: 5px; }\n" +
                "        .stat-card .value { font-size: 24px; font-weight: bold; color: #00ff00; }\n" +
                "        table { width: 100%; border-collapse: collapse; margin-top: 10px; }\n" +
                "        th { background: #1a1410; color: #ff9800; text-align: left; padding: 8px; font-size: 12px; border: 1px solid #5a4a35; }\n" +
                "        td { padding: 8px; border: 1px solid #5a4a35; font-size: 12px; background: rgba(26,20,16,0.5); }\n" +
                "        tr:hover td { background: rgba(255,152,0,0.2); }\n" +
                "        .action-btn { padding: 4px 10px; border: 2px solid #5a4a35; cursor: pointer; font-size: 11px; font-family: 'Courier New', monospace; margin-right: 3px; }\n" +
                "        .kick-btn { background: #8b0000; color: #ffff00; }\n" +
                "        .kick-btn:hover { background: #b30000; }\n" +
                "        .view-btn { background: #1a5a1a; color: #00ff00; }\n" +
                "        .view-btn:hover { background: #267326; }\n" +
                "        .console { background: #000; border: 3px solid #5a4a35; padding: 15px; height: 400px; overflow-y: auto; margin-bottom: 10px; font-size: 13px; }\n" +
                "        .console-line { margin: 2px 0; }\n" +
                "        .console-line.success { color: #00ff00; }\n" +
                "        .console-line.error { color: #ff0000; }\n" +
                "        .console-line.info { color: #00ffff; }\n" +
                "        .console-line.warning { color: #ffff00; }\n" +
                "        .console-input { display: flex; gap: 5px; }\n" +
                "        .console-input input { flex: 1; background: #000; border: 2px solid #5a4a35; color: #00ff00; padding: 8px; font-family: 'Courier New', monospace; font-size: 13px; }\n" +
                "        .console-input button { background: #1a5a1a; color: #00ff00; border: 2px solid #5a4a35; padding: 8px 15px; cursor: pointer; font-family: 'Courier New', monospace; font-size: 13px; }\n" +
                "        .console-input button:hover { background: #267326; }\n" +
                "        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 1000; }\n" +
                "        .modal-content { background: linear-gradient(180deg, #584123 0%, #3d2c18 100%); margin: 5% auto; padding: 25px; border: 3px solid #8b7355; border-radius: 5px; max-width: 600px; color: #ffff00; box-shadow: 0 0 50px rgba(0,0,0,0.8); }\n" +
                "        .modal-content h3 { color: #ff9800; margin-bottom: 15px; text-shadow: 1px 1px 2px #000; }\n" +
                "        .close { float: right; font-size: 24px; cursor: pointer; color: #ff9800; font-weight: bold; }\n" +
                "        .stat-row { display: flex; justify-content: space-between; padding: 8px; border-bottom: 1px solid #5a4a35; background: rgba(26,20,16,0.5); }\n" +
                "        .stat-row:nth-child(even) { background: rgba(26,20,16,0.8); }\n" +
                "        .stat-row .label { color: #ff9800; }\n" +
                "        .stat-row .value { color: #00ff00; font-weight: bold; }\n" +
                "        .right-panel { grid-column: 2; }\n" +
                "        .main-panel { grid-column: 1; display: flex; flex-direction: column; gap: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='header'>\n" +
                "        <h1>" + Settings.SERVER_NAME + "</h1>\n" +
                "    </div>\n" +
                "    <div class='container'>\n" +
                "        <div class='main-panel'>\n" +
                "            <div class='panel'>\n" +
                "                <div class='panel-title'>COMMAND CONSOLE</div>\n" +
                "                <div class='console' id='consoleOutput'></div>\n" +
                "                <div class='console-input'>\n" +
                "                    <input type='text' id='commandInput' placeholder='Enter command... (e.g., /maxed Joey, /item 995 1000000)' onkeypress='if(event.key===\"Enter\")executeCommand()' />\n" +
                "                    <button onclick='executeCommand()'>Execute</button>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class='panel'>\n" +
                "                <div class='panel-title'>ONLINE PLAYERS</div>\n" +
                "                <table>\n" +
                "                    <thead>\n" +
                "                        <tr>\n" +
                "                            <th>Username</th>\n" +
                "                            <th>Rights</th>\n" +
                "                            <th>Combat</th>\n" +
                "                            <th>Location</th>\n" +
                "                            <th>Actions</th>\n" +
                "                        </tr>\n" +
                "                    </thead>\n" +
                "                    <tbody id='playersTable'>\n" +
                "                    </tbody>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class='right-panel'>\n" +
                "            <div class='panel'>\n" +
                "                <div class='panel-title'>SERVER STATUS</div>\n" +
                "                <div class='stats-grid'>\n" +
                "                    <div class='stat-card'>\n" +
                "                        <div class='label'>ONLINE</div>\n" +
                "                        <div class='value' id='onlinePlayers'>0</div>\n" +
                "                    </div>\n" +
                "                    <div class='stat-card'>\n" +
                "                        <div class='label'>TOTAL</div>\n" +
                "                        <div class='value' id='totalPlayers'>0</div>\n" +
                "                    </div>\n" +
                "                    <div class='stat-card'>\n" +
                "                        <div class='label'>UPTIME</div>\n" +
                "                        <div class='value' id='uptime' style='font-size:16px'>0h</div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class='panel' style='margin-top: 20px;'>\n" +
                "                <div class='panel-title'>COMMAND HELP</div>\n" +
                "                <div style='font-size: 11px; color: #00ff00; line-height: 1.6;'>\n" +
                "                    <div><span style='color: #ff9800;'>/item</span> [player] [id] [qty]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/tele</span> [player] [x] [y]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/maxed</span> [player]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/setlevel</span> [player] [skill] [lvl]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/npc</span> [id] [x] [y]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/object</span> [id] [x] [y]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/kick</span> [player]</div>\n" +
                "                    <div><span style='color: #ff9800;'>/msg</span> [player] [message]</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div id='playerModal' class='modal'>\n" +
                "        <div class='modal-content'>\n" +
                "            <span class='close' onclick='closeModal()'>&times;</span>\n" +
                "            <h3 id='modalPlayerName'>Player Stats</h3>\n" +
                "            <div id='modalContent'></div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        let startTime = Date.now();\n" +
                "        function addConsoleLog(msg, type='info') {\n" +
                "            const console = document.getElementById('consoleOutput');\n" +
                "            const line = document.createElement('div');\n" +
                "            line.className = 'console-line ' + type;\n" +
                "            const timestamp = new Date().toLocaleTimeString();\n" +
                "            line.textContent = '[' + timestamp + '] ' + msg;\n" +
                "            console.appendChild(line);\n" +
                "            console.scrollTop = console.scrollHeight;\n" +
                "        }\n" +
                "        function executeCommand() {\n" +
                "            const input = document.getElementById('commandInput');\n" +
                "            const cmd = input.value.trim();\n" +
                "            if (!cmd) return;\n" +
                "            addConsoleLog('> ' + cmd, 'warning');\n" +
                "            fetch('/api/command?cmd=' + encodeURIComponent(cmd), {method: 'POST'})\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    addConsoleLog(data.message, data.success ? 'success' : 'error');\n" +
                "                    if (data.success) loadPlayers();\n" +
                "                })\n" +
                "                .catch(err => addConsoleLog('Error: ' + err.message, 'error'));\n" +
                "            input.value = '';\n" +
                "        }\n" +
                "        function loadPlayers() {\n" +
                "            fetch('/api/players')\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    document.getElementById('onlinePlayers').textContent = data.players.length;\n" +
                "                    document.getElementById('totalPlayers').textContent = data.totalRegistered;\n" +
                "                    const tbody = document.getElementById('playersTable');\n" +
                "                    tbody.innerHTML = '';\n" +
                "                    data.players.forEach(p => {\n" +
                "                        const row = `<tr>\n" +
                "                            <td><strong>${p.name}</strong></td>\n" +
                "                            <td>${p.rights}</td>\n" +
                "                            <td>${p.combatLevel}</td>\n" +
                "                            <td>(${p.x}, ${p.y})</td>\n" +
                "                            <td>\n" +
                "                                <button class='action-btn view-btn' onclick='viewPlayer(\"${p.name}\")'>View</button>\n" +
                "                                <button class='action-btn kick-btn' onclick='kickPlayer(\"${p.name}\")'>Kick</button>\n" +
                "                            </td>\n" +
                "                        </tr>`;\n" +
                "                        tbody.innerHTML += row;\n" +
                "                    });\n" +
                "                });\n" +
                "        }\n" +
                "        function viewPlayer(name) {\n" +
                "            fetch('/api/stats?name=' + name)\n" +
                "                .then(res => res.json())\n" +
                "                .then(data => {\n" +
                "                    document.getElementById('modalPlayerName').textContent = data.name + \"'s Stats\";\n" +
                "                    let html = '';\n" +
                "                    html += `<div class='stat-row'><span class='label'>Combat Level:</span><span class='value'>${data.combatLevel}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Total Level:</span><span class='value'>${data.totalLevel}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Attack:</span><span class='value'>${data.attack}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Strength:</span><span class='value'>${data.strength}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Defence:</span><span class='value'>${data.defence}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Hitpoints:</span><span class='value'>${data.hitpoints}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Prayer:</span><span class='value'>${data.prayer}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Magic:</span><span class='value'>${data.magic}</span></div>`;\n" +
                "                    html += `<div class='stat-row'><span class='label'>Ranged:</span><span class='value'>${data.ranged}</span></div>`;\n" +
                "                    document.getElementById('modalContent').innerHTML = html;\n" +
                "                    document.getElementById('playerModal').style.display = 'block';\n" +
                "                });\n" +
                "        }\n" +
                "        function kickPlayer(name) {\n" +
                "            if (confirm('Kick player ' + name + '?')) {\n" +
                "                fetch('/api/kick?name=' + name, {method: 'POST'})\n" +
                "                    .then(() => { loadPlayers(); addConsoleLog('Kicked player: ' + name, 'success'); });\n" +
                "            }\n" +
                "        }\n" +
                "        function closeModal() {\n" +
                "            document.getElementById('playerModal').style.display = 'none';\n" +
                "        }\n" +
                "        function updateUptime() {\n" +
                "            const hours = Math.floor((Date.now() - startTime) / 3600000);\n" +
                "            const mins = Math.floor((Date.now() - startTime) / 60000) % 60;\n" +
                "            document.getElementById('uptime').textContent = hours + 'h ' + mins + 'm';\n" +
                "        }\n" +
                "        addConsoleLog('Server console initialized', 'success');\n" +
                "        addConsoleLog('Type commands to execute server actions', 'info');\n" +
                "        loadPlayers();\n" +
                "        setInterval(loadPlayers, 5000);\n" +
                "        setInterval(updateUptime, 1000);\n" +
                "        updateUptime();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
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
            
            if (npcType == -1 || x == -1 || y == -1) {
                sendJson(exchange, 400, "{\"error\":\"Missing npc_type, x, or y\"}");
                return;
            }
            
            GameEngine.pendingActions.add(() -> {
                GameEngine.npcHandler.spawnNpc2(npcType, x, y, height, 1, 100, 1, 1, 1);
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
            });

            sendJson(exchange, 200, "{\"success\":true}");
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

