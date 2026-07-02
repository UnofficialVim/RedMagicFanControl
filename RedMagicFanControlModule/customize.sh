#!/system/bin/sh

# Find the fan control path
FAN_DIR=$(find /sys -name "fan_enable" 2>/dev/null | head -n 1 | sed 's|/fan_enable||')
if [ -n "$FAN_DIR" ]; then
    ui_print "Found fan control path: $FAN_DIR"
    :
else
    ui_print "Fan path missing"
    ui_print "Aborting installation"
    abort
fi

# Set the fan control path in the json file
sed -i "s|\"fan_path\": \".*\"|\"fan_path\": \"$FAN_DIR\"|" "$MODPATH/config/config.json"

