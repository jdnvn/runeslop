from dotenv import load_dotenv
import requests
import boto3
import json
import threading
import time
from collections import deque

load_dotenv()

class Tool:
    needs_response = False
    
    def tool_spec(self):
        return {
            "name": self.name,
            "description": self.description,
            "input_schema": self.input_schema
        }
    
    def execute(self, args, api_base):
        raise NotImplementedError()

class NPCChatTool(Tool):
    def __init__(self):
        self.name = "npc_say"
        self.description = "Make an NPC say something in the game world (overhead chat)."
        self.input_schema = {
            "type": "object",
            "properties": {
                "npc_id": {"type": "number", "description": "The NPC's server instance ID (from get_npcs)"},
                "npc_name": {"type": "string", "description": "The NPC's name you want to show up in the game chat history"},
                "text": {"type": "string", "description": "What the NPC should say"}
            },
            "required": ["npc_id", "npc_name", "text"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/npc_say", json=args, timeout=5)
        return resp.json()

class SpawnNPCTool(Tool):
    needs_response = True
    
    def __init__(self):
        self.name = "spawn_npc"
        self.description = "Spawn an NPC at a location with an optional custom name."
        self.input_schema = {
            "type": "object",
            "properties": {
                "npc_type": {"type": "number", "description": "NPC type ID. Use search_npcs_by_name to find the ID if you don't know it."},
                "x": {"type": "number"},
                "y": {"type": "number"},
                "height": {"type": "number", "description": "Height level, default 0"},
                "name": {"type": "string", "description": "Custom display name for this NPC (optional)"},
                "combat_level": {"type": "number", "description": "Custom combat level to display (optional, -1 for default)"}
            },
            "required": ["npc_type", "x", "y"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/spawn_npc", json=args, timeout=5)
        return resp.json()

class SearchNPCsByNameTool(Tool):
    def __init__(self):
        self.name = "search_npcs_by_name"
        self.description = "Search for supported NPC types by name. Use this to find the NPC Type ID when spawning a desired NPC."
        self.input_schema = {
            "type": "object",
            "properties": {
                "name": {"type": "string", "description": "The name of the NPC to search for. Case insensitive, will partial match."}
            },
            "required": ["name"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/search_npcs_by_name", json=args, timeout=5)
        print(f"[Search NPCs by Name] {resp.text}")
        return resp.json()

class DialogueTool(Tool):
    def __init__(self):
        self.name = "send_dialogue"
        self.description = "Send a dialogue popup to a player. Use | to separate lines (max 4). Do not send multiple dialogue popups in a row, the user must send a dialogue_continue event or dialogue_option event before sending another dialogue popup."
        self.input_schema = {
            "type": "object",
            "properties": {
                "player": {"type": "string", "description": "Player name"},
                "npc_name": {"type": "string", "description": "Name shown in dialogue box"},
                "npc_id": {"type": "number", "description": "NPC ID for chat head. Use get_npcs to find the ID if you don't know it."},
                "text": {"type": "string", "description": "Dialogue text. Use | for line breaks."}
            },
            "required": ["player", "npc_id", "text"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/dialogue", json=args, timeout=5)
        return resp.json()

class GetPlayersTool(Tool):
    needs_response = True
    
    def __init__(self):
        self.name = "get_players"
        self.description = "Get all online players with their positions."
        self.input_schema = {"type": "object", "properties": {}}
    
    def execute(self, args, api_base):
        resp = requests.get(f"{api_base}/agent/get_players", timeout=5)
        return resp.json()

class SendPlayerMessageTool(Tool):
    def __init__(self):
        self.name = "send_player_message"
        self.description = "Send a message to a player."
        self.input_schema = {
            "type": "object",
            "properties": {
                "player_name": {"type": "string"},
                "message": {"type": "string"}
            },
            "required": ["player_name", "message"]
        }

    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/api/players/send_message", json=args, timeout=5)
        return resp.json()

class GetNPCsTool(Tool):
    needs_response = True
    
    def __init__(self):
        self.name = "get_npcs"
        self.description = "Get all spawned NPCs with their IDs and positions."
        self.input_schema = {"type": "object", "properties": {}}
    
    def execute(self, args, api_base):
        resp = requests.get(f"{api_base}/agent/get_npcs", timeout=5)
        return resp.json()

class GiveItemTool(Tool):
    def __init__(self):
        self.name = "give_item"
        self.description = "Give an item to a player."
        self.input_schema = {
            "type": "object",
            "properties": {
                "player": {"type": "string"},
                "item_id": {"type": "number"},
                "amount": {"type": "number", "description": "Default 1"}
            },
            "required": ["player", "item_id"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/give_item", json=args, timeout=5)
        return resp.json()

class SearchItemsByNameTool(Tool):
    def __init__(self):
        self.name = "search_items_by_name"
        self.description = "Search for supported items by name. Use this to find the item ID when giving a desired item to a player."
        self.input_schema = {
            "type": "object",
            "properties": {
                "name": {"type": "string", "description": "The name of the item to search for. Case insensitive, will partial match."}
            },
            "required": ["name"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/search_items_by_name", json=args, timeout=5)
        return resp.json()

class TeleportTool(Tool):
    def __init__(self):
        self.name = "teleport"
        self.description = "Teleport a player to a location."
        self.input_schema = {
            "type": "object",
            "properties": {
                "player": {"type": "string"},
                "x": {"type": "number"},
                "y": {"type": "number"},
                "height": {"type": "number", "description": "Default 0"}
            },
            "required": ["player", "x", "y"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/teleport", json=args, timeout=5)
        return resp.json()

class WalkNPCTool(Tool):
    def __init__(self):
        self.name = "walk_npc"
        self.description = "Walk an NPC to a nearby location."
        self.input_schema = {
            "type": "object",
            "properties": {
                "npc_id": {"type": "number", "description": "NPC ID"},
                "x": {"type": "number"},
                "y": {"type": "number"}
            },
            "required": ["npc_id", "x", "y"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/walk_npc", json=args, timeout=5)
        return resp.json()

class TeleportNPCTool(Tool):
    def __init__(self):
        self.name = "teleport_npc"
        self.description = "Teleport an NPC to a location."
        self.input_schema = {
            "type": "object",
            "properties": {
                "npc_id": {"type": "number", "description": "NPC ID"},
                "x": {"type": "number"},
                "y": {"type": "number"}
            },
            "required": ["npc_id", "x", "y"]
        }

    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/teleport_npc", json=args, timeout=5)
        return resp.json()


class GetNPCInfoTool(Tool):

    def __init__(self):
        self.name = "get_npc_info"
        self.description = "Get information about an NPC."
        self.input_schema = {
            "type": "object",
            "properties": {
                "npc_id": {"type": "number", "description": "NPC ID"}
            },
            "required": ["npc_id"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/get_npc_info", json=args, timeout=5)
        return resp.json()

class GetObjectsTool(Tool):
    needs_response = True
    
    def __init__(self):
        self.name = "get_objects"
        self.description = "Get all spawned objects with their IDs and positions."
        self.input_schema = {"type": "object", "properties": {}}
    
    def execute(self, args, api_base):
        resp = requests.get(f"{api_base}/agent/get_objects", timeout=5)
        return resp.json()

class AddObjectTool(Tool):
    def __init__(self):
        self.name = "add_object"
        self.description = "Add an object to the game world."
        self.input_schema = {
            "type": "object",
            "properties": {
                "object_id": {"type": "number", "description": "Object ID"},
                "x": {"type": "number"},
                "y": {"type": "number"},
                "height": {"type": "number", "description": "Default 0"},
                "face": {"type": "number", 
                        "description": "0=North, 1=East, 2=South, 3=West"},
                "type": {"type": "number", "description": "Default 10"}
            },
            "required": ["object_id", "x", "y", "height", "face", "type"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/add_object", json=args, timeout=5)
        return resp.json()

class RemoveObjectTool(Tool):
    def __init__(self):
        self.name = "remove_object"
        self.description = "Remove an object from the game world."
        self.input_schema = {
            "type": "object",
            "properties": {
                "object_id": {"type": "number", "description": "Object ID"}
            },
            "required": ["object_id"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/remove_object", json=args, timeout=5)
        return resp.json()

class GetObjectInfoTool(Tool):
    def __init__(self):
        self.name = "get_object_info"
        self.description = "Get information about an object."
        self.input_schema = {
            "type": "object",
            "properties": {
                "object_id": {"type": "number", "description": "Object ID"}
            },
            "required": ["object_id"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/get_object_info", json=args, timeout=5)
        return resp.json()

class SendOptionsTool(Tool):
    def __init__(self):
        self.name = "send_options"
        self.description = "Show dialogue options for the player to choose from (2-5 options). You'll receive a 'dialogue_option' event when they select an option. If the option is assumed to be a 'exit' type option, you should not send another dialogue popup."
        self.input_schema = {
            "type": "object",
            "properties": {
                "player": {"type": "string", "description": "Player name"},
                "options": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "2-5 option strings for the player to choose from",
                    "minItems": 2,
                    "maxItems": 5
                }
            },
            "required": ["player", "options"]
        }
    
    def execute(self, args, api_base):
        resp = requests.post(f"{api_base}/agent/send_options", json=args, timeout=5)
        return resp.json()

class QuitTool(Tool):
    def __init__(self):
        self.name = "quit"
        self.description = "End the agent session when the task is complete."
        self.input_schema = {"type": "object", "properties": {}}
    
    def execute(self, args, api_base):
        print("Agent finished!")
        exit(0)



class OSRSAgent:
    API_BASE = "http://localhost:4321"
    AWS_REGION = "us-east-1"
    MODEL_ID = "us.anthropic.claude-sonnet-4-20250514-v1:0"
    
    def __init__(self):
        self.bedrock = boto3.client("bedrock-runtime", region_name=self.AWS_REGION)
        
        self.tools = [
            NPCChatTool(),
            SpawnNPCTool(),
            DialogueTool(),
            GetPlayersTool(),
            SendPlayerMessageTool(),
            GetNPCsTool(),
            GetNPCInfoTool(),
            TeleportNPCTool(),
            SearchNPCsByNameTool(),
            WalkNPCTool(),
            GiveItemTool(),
            SearchItemsByNameTool(),
            TeleportTool(),
            SendOptionsTool(),
            GetObjectsTool(),
            AddObjectTool(),
            RemoveObjectTool(),
            GetObjectInfoTool(),
            QuitTool()
        ]
        self.tools_by_name = {t.name: t for t in self.tools}
        self.tool_specs = [t.tool_spec() for t in self.tools]
        
        self.messages = []
        
        self.event_queue = deque()
        self.batch_delay = 0.1
        self.processing_lock = threading.Lock()
    
    def run(self, task_description):
        self.messages = [{
            "role": "user",
            "content": [{"type": "text", "text": self._prompt(task_description)}]
        }]
        
        self._call_claude_and_handle()
        
        processor = threading.Thread(target=self._event_processor, daemon=True)
        processor.start()
        
        self._listen_for_events()
    
    def _listen_for_events(self):
        """Listen for SSE events from the server"""
        print("[SSE] Connecting to event stream...")
        event_type = None
        
        try:
            with requests.get(f"{self.API_BASE}/agent/events", stream=True, timeout=None) as response:
                print("[SSE] Connected!")
                
                for line in response.iter_lines():
                    if not line:
                        continue
                    
                    line = line.decode('utf-8')
                    
                    if line.startswith("event:"):
                        event_type = line[6:].strip()
                    elif line.startswith("data:"):
                        data = line[5:].strip()
                        if event_type and event_type not in ("connected", "ping"):
                            self.event_queue.append((event_type, data))
                            print(f"[Event queued] {event_type}: {data}")
                        event_type = None
                        
        except Exception as e:
            print(f"[SSE] Error: {e}")
    
    def _event_processor(self):
        while True:
            if not self.event_queue:
                time.sleep(0.05)
                continue

            # Wait a bit more to batch events that come in quick succession
            time.sleep(self.batch_delay)
            
            events = []
            while self.event_queue:
                events.append(self.event_queue.popleft())
            
            if not events:
                continue
            
            with self.processing_lock:
                self._handle_event_batch(events)
    
    def _handle_event_batch(self, events):
        if len(events) == 1:
            event_type, data = events[0]
            print(f"[Processing 1 event]")
            event_text = f"Game event ({event_type}): {data}"
        else:
            print(f"[Processing {len(events)} events]")
            event_lines = [f"- {event_type}: {data}" for event_type, data in events]
            event_text = f"Multiple game events:\n" + "\n".join(event_lines)
        
        self.messages.append({
            "role": "user",
            "content": [{"type": "text", "text": event_text}]
        })
        
        self._call_claude_and_handle()
    
    def _call_claude_and_handle(self):
        while True:
            response = self._call_claude()
            
            if response is None:
                print("[Error] Claude API call failed, skipping this event")
                return
            
            self._print_response(response)
            
            if response["stop_reason"] == "tool_use":
                tool_results = self._execute_tools(response["content"])
                
                # Only append if content is non-empty
                if response["content"]:
                    self.messages.append({"role": "assistant", "content": response["content"]})
                if tool_results:
                    self.messages.append({"role": "user", "content": tool_results})
                else:
                    # No tool results, break to avoid empty message
                    print("[Warning] No tool results, ending turn")
                    break
            else:
                # end_turn - Claude is done, add to history and return
                if response["content"]:
                    self.messages.append({"role": "assistant", "content": response["content"]})
                break
    
    def _execute_tools(self, content):
        results = []
        
        for item in content:
            if item["type"] != "tool_use":
                continue
            
            tool_name = item["name"]
            tool_args = item["input"]
            tool_id = item["id"]
            
            tool = self.tools_by_name.get(tool_name)
            if not tool:
                results.append({
                    "type": "tool_result",
                    "tool_use_id": tool_id,
                    "content": json.dumps({"error": f"Unknown tool: {tool_name}"})
                })
                continue
            
            print(f"[Tool] {tool_name}: {tool_args}")
            
            try:
                result = tool.execute(tool_args, self.API_BASE)
                print(f"[Tool Result] {result}")
                results.append({
                    "type": "tool_result",
                    "tool_use_id": tool_id,
                    "content": json.dumps(result)
                })
            except Exception as e:
                print(f"[Tool Error] {e}")
                results.append({
                    "type": "tool_result",
                    "tool_use_id": tool_id,
                    "content": json.dumps({"error": str(e)})
                })
        
        return results
    
    def _call_claude(self):
        """Call Claude via Bedrock"""
        # Filter out any messages with empty content
        valid_messages = []
        for msg in self.messages:
            content = msg.get("content")
            if content is None:
                continue
            # Check if content is a non-empty list or non-empty string
            if isinstance(content, list) and len(content) == 0:
                continue
            if isinstance(content, str) and len(content.strip()) == 0:
                continue
            valid_messages.append(msg)
        
        body = json.dumps({
            "messages": valid_messages,
            "max_tokens": 4096,
            "temperature": 0.7,
            "anthropic_version": "bedrock-2023-05-31",
            "tools": self.tool_specs
        })

        max_retries = 5
        for attempt in range(max_retries):
            try:
                response = self.bedrock.invoke_model(
                    modelId=self.MODEL_ID,
                    contentType="application/json",
                    body=body
                )
                return json.loads(response['body'].read())
            except self.bedrock.exceptions.ThrottlingException as e:
                if attempt == max_retries - 1:
                    print(f"Max retries ({max_retries}) reached for throttling: {e}")
                    return None
                wait_time = 2 ** attempt
                print(f"Throttling, retrying in {wait_time}s (attempt {attempt + 1}/{max_retries}): {e}")
                time.sleep(wait_time)
            except Exception as e:
                print(f"Error calling bedrock: {e}")
                return None
        
        return None
    
    def _prompt(self, task):
        return f"""You are the AI brain for an Old School RuneScape private server.

Your task: {task}

You control the game using tools: spawn NPCs, make them talk, send dialogues to players, etc.
You receive game events like player chat messages and dialogue clicks.

Guidelines:
- When a player chats, you might want to respond via an NPC
- Call get_npcs to find NPC instance IDs for npc_say and dialogues
- Only use dialogues with an NPC when the player has clicked on said NPC, otherwise respond to user chats with npc_say.
- If you use npc_say, make sure you are within range of the player. You can figure this out by comparing the player's position to the NPC's position. If you aren't within range, walk or teleport to the player you want to talk to.
- Be creative and roleplay appropriately! Make the personality like classic Runescape dialogue, witty english humor, dry and not cringe. Generally straightforward fantasy prose. Limit the use of excessive asterisk roleplay, you can do it but don't overdo it.

When the task is complete, call the quit tool."""
    
    def _print_response(self, response):
        if response is None:
            print("=" * 50)
            print("[Error] No response from Claude")
            print("=" * 50)
            return
        
        print("=" * 50)
        print(f"Agent decision: ({response.get('stop_reason', 'unknown')}):")
        for item in response.get("content", []):
            if item["type"] == "text":
                print(f"  {item['text']}")
            elif item["type"] == "tool_use":
                print(f"  Calling tool {item['name']} with args: {item['input']}")
        print("=" * 50)


if __name__ == "__main__":
    agent = OSRSAgent()
    task = "Roleplay as a wise wizard NPC. Greet players who chat, tell jokes if asked, and be friendly."
    # task = "Roleplay as an evil wizard NPC. Try to trick players into doing your bidding. Be cunning and devious, but also friendly and engaging. If it comes to it, you should be willing to kill players if they get in your way."
    
    # task = "Just control the game dialogue and lead the player through a quest of your own creation. If they click on things, you should send a message to the user in the chat box describing it. Treat this like a text based adventure game, but with Runescape items and NPCs you control as well. Make it fun and engaging!"
    print(f"Starting: {task}")
    print("-" * 50)
    agent.run(task)
