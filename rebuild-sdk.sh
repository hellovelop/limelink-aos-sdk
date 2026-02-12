#!/bin/bash
# Script to rebuild SDK and example app
# Usage: ./rebuild-sdk.sh

set -e  # Exit on error

echo "🔧 Building and publishing SDK to Maven Local..."
./gradlew clean publishToMavenLocal

echo ""
echo "📱 Rebuilding example app..."
cd example
./gradlew clean :app:assembleDebug

echo ""
echo "✅ Done! SDK and example app built successfully."
echo ""
echo "To run the example app:"
echo "  cd example"
echo "  ./gradlew :app:installDebug"
