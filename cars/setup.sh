#!/bin/bash
set -o errexit
set -o nounset

sudo apt-get --assume-yes install \
    cowsay \
    jp2a \
    python3 \
    streamer
