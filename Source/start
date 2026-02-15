#!/bin/bash
# RuneScape Private Server - Server Launcher

echo "=========================================="
echo "  Clean PI - Server Launcher"
echo "=========================================="
echo ""

# Navigate to the Source directory
cd "$(dirname "$0")/Source"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java JDK 8 or higher"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "ERROR: bin directory not found"
    echo "Please compile the server first"
    exit 1
fi

# Check if deps directory exists
if [ ! -d "deps" ]; then
    echo "ERROR: deps directory not found"
    echo "Required JAR files are missing"
    exit 1
fi

echo "Starting server on port 43594..."
echo "Press Ctrl+C to stop the server"
echo ""
echo "=========================================="
echo ""

# Start the server
java -cp "bin:deps/*" RS2.GameEngine

