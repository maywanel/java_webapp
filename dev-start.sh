#!/bin/bash
# Development script with auto-reload

echo "🚀 Starting Spring Boot with enhanced development mode..."

# Kill any existing processes
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "browser-sync" 2>/dev/null || true

# Start Spring Boot in background
echo "📦 Starting Spring Boot application..."
./mvnw spring-boot:run &
SPRING_PID=$!

# Wait for Spring Boot to start
echo "⏳ Waiting for Spring Boot to start..."
sleep 10

# Check if browser-sync is available
if command -v browser-sync &> /dev/null; then
    echo "🌐 Starting Browser-Sync for live reload..."
    browser-sync start --proxy "localhost:8080" \
        --files "src/main/resources/templates/**/*" \
        --files "src/main/resources/static/**/*" \
        --reload-delay 1000 \
        --reload-debounce 500 &
    BROWSERSYNC_PID=$!
    echo "✅ Development server ready!"
    echo "🔗 Open: http://localhost:3000 (with live reload)"
    echo "🔗 Original: http://localhost:8080"
    echo "📁 Watching: templates/ and static/ directories"
else
    echo "💡 Install browser-sync for enhanced live reload:"
    echo "   npm install -g browser-sync"
    echo "🔗 Open: http://localhost:8080"
fi

# Function to cleanup on exit
cleanup() {
    echo "🛑 Stopping development servers..."
    kill $SPRING_PID 2>/dev/null || true
    if [ ! -z "$BROWSERSYNC_PID" ]; then
        kill $BROWSERSYNC_PID 2>/dev/null || true
    fi
    exit 0
}

# Trap cleanup function on script exit
trap cleanup SIGINT SIGTERM

# Wait for processes
wait