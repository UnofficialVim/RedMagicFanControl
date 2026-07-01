#!/system/bin/sh

MODDIR=${0%/*}/..

LOG="/data/adb/redmagic-fan-control.log"

FAN="/sys/kernel/fan/fan_enable"


log()
{
    printf "$(date): $1" >> "$LOG" \n
}


if [ ! -e "$FAN" ]; then

    log "Fan path missing: $FAN"
    exit 1

fi


log "Fan daemon started"

while true
do

    # test only
    log "$(cat $FAN)"

    sleep 30

done