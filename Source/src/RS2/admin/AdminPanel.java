package RS2.admin;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import RS2.model.player.PlayerHandler;
import RS2.Settings;
import RS2.model.player.Client;

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
            
            server.setExecutor(null); // Creates a default executor
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
                String[] parts = cmd.trim().split("\\s+");
                if (parts.length == 0) return "Empty command";
                
                String action = parts[0].startsWith("/") ? parts[0].substring(1) : parts[0];
                return CommandHandler.executeServerCommand(action);
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
}

