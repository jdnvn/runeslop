# Clean PI - Quick Start Guide

## 🚀 Fastest Way to Run Your Server

### For Mac/Linux Users:

**Step 1:** Open Terminal and start the server:
```bash
cd "/Users/joey/Clean PI"
./start-server.sh
```

**Step 2:** Open a NEW Terminal window and start the client:
```bash
cd "/Users/joey/Clean PI"
./start-client.sh
```

## 📋 Manual Method

If the scripts don't work, you can start everything manually:

### Start Server (Mac/Linux):
```bash
cd "/Users/joey/Clean PI/Source"
java -cp "bin:deps/*" RS2.GameEngine
```

### Start Server (Windows):
```cmd
cd "C:\path\to\Clean PI\Source"
java -cp "bin;deps/*" RS2.GameEngine
```

### Start Client (Mac/Linux):
```bash
cd "/Users/joey/Clean PI/Client"
java client
```

### Start Client (Windows):
```cmd
cd "C:\path\to\Clean PI\Client"
java client
```

---

## ✅ System Requirements

- **Java 8 or higher** (Check with: `java -version`)
- The server must be running BEFORE starting the client
- Default port: **43594**

---

## 🎮 First Time Setup

1. Start the server first (wait for "Took X milliseconds to launch")
2. Start the client
3. Enter any username and password
4. Your account will be created automatically!

---

## ⚙️ Configuration

- **Server settings:** Edit `Source/src/RS2/Settings.java`
- **Server port:** Edit `Source/src/RS2/GameEngine.java` (line 121)
- **Client connection:** Edit `Client/client.java` (line 12115)

---

## 🆘 Troubleshooting

**Can't find Java?**
- Install Java JDK 8: https://www.oracle.com/java/technologies/downloads/

**Port already in use?**
- Close any other applications using port 43594
- Or change the port in `GameEngine.java`

**Client won't connect?**
- Make sure the server started successfully
- Check that you see "Took X milliseconds to launch" in server console
- Verify the client connects to 127.0.0.1

---

## 📚 More Information

See `HOW_TO_RUN.md` for detailed instructions and advanced configuration.

