#!/bin/bash
set -o errexit
set -o nounset

sudo apt-get --assume-yes install \
    python-setuptools

sudo easy_install markdown

