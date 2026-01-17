# Runeslop

### For Mac/Linux Users:

**Step 1:** Open Terminal and start the server:
```bash
git clone https://github.com/jdnvn/runeslop.git && cd runeslop
./start-server.sh
```

**Step 2:** Open a NEW Terminal window and start the client:
```bash
cd ~/path/to/runeslop
./start-client.sh
```

## Manual Method

If the scripts don't work, you can start everything manually:

### Start Server (Mac/Linux):
```bash
cd "~/path/to/runeslop/Source"
java -cp "bin:deps/*" RS2.GameEngine
```

### Start Server (Windows):
```cmd
cd "C:\path\to\runeslop\Source"
java -cp "bin;deps/*" RS2.GameEngine
```

### Start Client (Mac/Linux):
```bash
cd ~/path/to/runeslop/Client
java client
```

### Start Client (Windows):
```cmd
cd "C:\path\to\runeslop\Client"
java client
```

---

## System Requirements

- **Java 8 or higher** (Check with: `java -version`)
- The server must be running BEFORE starting the client
- Default port: **43594**

---

## First Time Setup

1. Start the server first (wait for "Took X milliseconds to launch")
2. Start the client
3. Enter any username and password
4. Your account will be created automatically!

---

## Configuration

- **Server settings:** Edit `Source/src/RS2/Settings.java`
- **Server port:** Edit `Source/src/RS2/GameEngine.java` (line 121)
- **Client connection:** Edit `Client/client.java` (line 12115)
