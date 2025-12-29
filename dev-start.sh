#!/bin/bash
set -euo pipefail

DEFAULT_APP_PORT=8080
APP_PORT=${APP_PORT:-}
PROXY_PORT=${PROXY_PORT:-3000}
WAIT_TIMEOUT=${WAIT_TIMEOUT:-60}
RELOAD_DELAY=${RELOAD_DELAY:-1000}
RELOAD_DEBOUNCE=${RELOAD_DEBOUNCE:-500}
SPRING_CMD=${SPRING_CMD:-"./mvnw spring-boot:run"}
WATCH_PATHS=(
    "src/main/resources/templates/**/*"
    "src/main/resources/static/**/*"
)

SPRING_PID=""
BROWSERSYNC_PID=""

log() {
    echo -e "$1"
}

cleanup() {
    log "🛑 Stopping development servers..."
    if [[ -n "${BROWSERSYNC_PID}" ]] && kill -0 "${BROWSERSYNC_PID}" >/dev/null 2>&1; then
        kill "${BROWSERSYNC_PID}" >/dev/null 2>&1 || true
    fi
    if [[ -n "${SPRING_PID}" ]] && kill -0 "${SPRING_PID}" >/dev/null 2>&1; then
        kill "${SPRING_PID}" >/dev/null 2>&1 || true
    fi
}

trap cleanup EXIT INT TERM

sanitize_port() {
    local raw="${1:-}"
    raw="${raw%%#*}"
    raw="${raw//\"/}"
    raw="${raw//\'/}"
    raw=$(echo "${raw}" | tr -d '[:space:]')
    echo "${raw}"
}

resolve_app_port() {
    if [[ -n "${APP_PORT}" ]]; then
        return
    fi

    if [[ -n "${SERVER_PORT:-}" ]]; then
        APP_PORT="${SERVER_PORT}"
        return
    fi

    local env_file=".env"
    if [[ -f "${env_file}" ]]; then
        local env_line env_port
        env_line=$(grep -E '^SERVER_PORT=' "${env_file}" | tail -n1 || true)
        if [[ -n "${env_line}" ]]; then
            env_port=$(sanitize_port "${env_line#*=}")
            if [[ -n "${env_port}" ]]; then
                APP_PORT="${env_port}"
                return
            fi
        fi
    fi

    local props_file="src/main/resources/application.properties"
    if [[ -f "${props_file}" ]]; then
        local prop_line prop_port
        prop_line=$(grep -E '^server\.port=' "${props_file}" | tail -n1 || true)
        if [[ -n "${prop_line}" ]]; then
            prop_port=$(sanitize_port "${prop_line#*=}")
            if [[ -n "${prop_port}" && "${prop_port}" != '${SERVER_PORT}' ]]; then
                APP_PORT="${prop_port}"
                return
            fi
        fi
    fi

    APP_PORT="${DEFAULT_APP_PORT}"
}

resolve_app_port

log "🚀 Starting Spring Boot with enhanced development mode..."
log "🎯 Using backend port ${APP_PORT}"

pkill -f "spring-boot:run" >/dev/null 2>&1 || true
pkill -f "browser-sync" >/dev/null 2>&1 || true

start_spring() {
    log "📦 Starting Spring Boot application..."
    eval "${SPRING_CMD}" &
    SPRING_PID=$!
}

check_port() {
    if command -v curl >/dev/null 2>&1 && curl -fs "http://localhost:${APP_PORT}" >/dev/null 2>&1; then
        return 0
    fi
    if command -v nc >/dev/null 2>&1 && nc -z localhost "${APP_PORT}" >/dev/null 2>&1; then
        return 0
    fi
    if command -v lsof >/dev/null 2>&1 && lsof -i tcp:"${APP_PORT}" >/dev/null 2>&1; then
        return 0
    fi
    return 1
}

wait_for_app() {
    log "⏳ Waiting for Spring Boot to start (timeout: ${WAIT_TIMEOUT}s)..."
    local waited=0
    until check_port; do
        sleep 1
        waited=$((waited + 1))
        if (( waited >= WAIT_TIMEOUT )); then
            log "❌ Spring Boot did not become ready in time."
            exit 1
        fi
    done
    log "✅ Spring Boot is listening on http://localhost:${APP_PORT}"
}

start_browser_sync() {
    if ! command -v browser-sync >/dev/null 2>&1; then
        log "💡 Install browser-sync for enhanced live reload: npm install -g browser-sync"
        log "🔗 Open: http://localhost:${APP_PORT}"
        return
    fi

    log "🌐 Starting Browser-Sync for live reload..."
    local args=(
        start
        --proxy "localhost:${APP_PORT}"
        --port "${PROXY_PORT}"
        --reload-delay "${RELOAD_DELAY}"
        --reload-debounce "${RELOAD_DEBOUNCE}"
    )
    for path in "${WATCH_PATHS[@]}"; do
        args+=(--files "${path}")
    done

    browser-sync "${args[@]}" &
    BROWSERSYNC_PID=$!

    log "✅ Development server ready!"
    log "🔗 Live reload: http://localhost:${PROXY_PORT}"
    log "🔗 Backend: http://localhost:${APP_PORT}"
    log "📁 Watching: ${WATCH_PATHS[*]}"
}

start_spring
wait_for_app
start_browser_sync

if [[ -n "${BROWSERSYNC_PID}" ]]; then
    wait "${SPRING_PID}" "${BROWSERSYNC_PID}"
else
    wait "${SPRING_PID}"
fi