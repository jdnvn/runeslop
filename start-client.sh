#!/bin/bash
# RuneScape Private Server - Client Launcher

echo "=========================================="
echo "  Clean PI - Client Launcher"
echo "=========================================="
echo ""

# Navigate to the Client directory
cd "$(dirname "$0")/Client"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java JDK 8 or higher"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Check if client.class exists
if [ ! -f "client.class" ]; then
    echo "ERROR: client.class not found"
    echo "Please compile the client first"
    exit 1
fi

echo "Starting client..."
echo "Connecting to 127.0.0.1:43594"
echo ""
echo "=========================================="
echo ""

# Start the client
java client

