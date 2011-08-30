#!/bin/bash
set -o errexit
set -o nounset

JSCH_DIR='jsch-0.1.44'
JSCH_URL='http://freefr.dl.sourceforge.net/project/jsch'
JSCH_URL="${JSCH_URL}/jsch/0.1.44/jsch-0.1.44.zip"
JSCH_ZIP='jsch-0.1.44.zip'

LIB_JSCH_DIR="`readlink -f $(dirname "$0")`/lib/"
cd /tmp
wget "${JSCH_URL}"
unzip "./${JSCH_ZIP}"
rm "./${JSCH_ZIP}"
cd "./${JSCH_DIR}"
ant dist
mv dist/lib/jsch-*.jar "${LIB_JSCH_DIR}/jsch-0.1.44.jar"
cd ..
rm -fr "./${JSCH_DIR}"
