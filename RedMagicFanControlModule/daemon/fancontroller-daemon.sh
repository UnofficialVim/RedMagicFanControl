#!/system/bin/sh

MODDIR=${0%/*}/..

LOGFILE="/data/adb/redmagic-fan-control.log"

log()
{
    echo "$(date) $1" >> "$LOGFILE"

    tail -n 500 "$LOGFILE" > "$LOGFILE.tmp"
    mv "$LOGFILE.tmp" "$LOGFILE"
}
log "$FAN_DIR"


if [ ! -e "$FAN_DIR" ]; then

    log "Fan path missing: $FAN_DIR"
    exit 1

fi

log "Fan daemon started"


FAN_SPEED_LEVEL="$$FAN_DIR/fan_speed_level"
while true; do
    if [ -e "$FAN_DIR" ]; then
        log "Current fan speed level: (cat "$FAN_SPEED_LEVEL")"
        if [ (cat "$FAN_SPEED_LEVEL") -lt 0 ] || [ (cat "$FAN_SPEED_LEVEL") -gt 5 ]; then
            log "Fan speed level out of range: (cat "$FAN_SPEED_LEVEL")"
            exit 1
        fi
    else
        log "Fan path missing: $FAN_DIR"
        exit 1
    fi
    sleep 5
done