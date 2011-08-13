#!/bin/bash
set -o errexit
set -o nounset

SDK_URL='http://dl.google.com/android/android-sdk_r12-linux_x86.tgz'
SDK_DIR='android-sdk-linux_x86'

cd /tmp
wget -O - "${SDK_URL}" | tar -xz
sudo mv "${SDK_DIR}" '/opt/'
