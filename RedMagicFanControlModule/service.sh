#!/system/bin/sh

# Magisk module service wrapper for redmagicfancontrold
# Starts the daemon in background
# file and redirects stdout/stderr to a log under the module dir.

MODDIR=${0%/*}
BIN="$MODDIR/bin/redmagicfancontrold"
LOGDIR="$MODDIR/logs"

mkdir -p "$LOGDIR"
log() { 
    echo "$(date +%Y-%m-%d\ %H:%M:%S) $1" >> "$LOGDIR/service.log"
}

# Check if the binary exists
if [ ! -f "$BIN" ]; then
    log "Binary not found: $BIN"
    log "Daemon will not start. Please check your installation."
    exit 1
fi

# Start the daemon in the background and redirect stdout/stderr to the log file
if [ ! -x "$BIN" ]; then
    log "Giving permissions to: $BIN"
    chmod 755 "$BIN" || {
        log "Failed to make binary executable"
        log "Daemon will not start. Please check your installation."
        exit 1
    }
fi
log "Starting redmagicfancontrold..."
"$BIN" >> "$LOGDIR/service.log" 2>&1 &
PID=$!
log "Started redmagicfancontrold with PID $PID"








