#!/system/bin/sh

MODDIR=${0%/*}

chmod 755 "$MODDIR/daemon/fan-daemon.sh"

"$MODDIR/daemon/fan-daemon.sh" &