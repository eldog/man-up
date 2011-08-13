#!/bin/bash
set -o errexit
set -o nounset

cd scripts

./python-2.5.2-setup.sh
./appengine-sdk-setup.sh
./nexus-one-setup.sh
./android-sdk-setup.sh
./spotify-setup.sh
./android-source-setup.sh

