#!/bin/bash
set -o errexit
set -o nounset

CHERRYPY_URL='http://download.cherrypy.org/cherrypy'
CHERRYPY_URL="${CHERRYPY_URL}/3.2.0/CherryPy-3.2.0-py3.1.egg"

sudo apt-get --assume-yes install \
    curl \
    ffmpeg \
    python3-setuptools \
    sox

sudo easy_install3 "${CHERRYPY_URL}"

