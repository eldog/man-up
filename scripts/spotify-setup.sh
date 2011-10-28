#!/bin/bash
set -o errexit
set -o nounset

SOURCE='deb http://repository.spotify.com stable non-free'

echo "$SOURCE" | sudo tee -a '/etc/apt/sources.list'
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 4E9CFF4E
sudo apt-get update
sudo apt-get --assume-yes install \
    spotify-client-qt \

