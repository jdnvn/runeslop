#!/bin/bash
echo "Project Insanity Client - Building..."
cd "$(dirname "$0")"

# Create bin directory structure
mkdir -p bin/sign

# Compile sign package first
javac -d bin sign/*.java

# Compile main source files
javac -cp bin -d bin src/*.java

echo "Build complete!"

