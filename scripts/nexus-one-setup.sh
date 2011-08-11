#!/bin/bash
set -o errexit
set -o nounset

RULE='SUBSYSTEM="usb", ATTR{idVendor}=="18d1", MODE="0666", GROUP="plugdev"'

echo "$RULE" | sudo tee '/etc/udev/rules.d/51-android.rules'
sudo service udev restart
