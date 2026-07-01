#!/system/bin/sh

MODDIR=${0%/*}

chmod 755 "$MODDIR/daemon/fancontroller-daemon.sh"

"$MODDIR/daemon/fancontroller-daemon.sh" &