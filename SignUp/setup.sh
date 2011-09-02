#!/bin/bash
set -o errexit
set -o nounset

JSCH_DIR='jsch-0.1.44'
JSCH_URL='http://freefr.dl.sourceforge.net/project/jsch'
JSCH_URL="${JSCH_URL}/jsch/0.1.44/jsch-0.1.44.zip"
JSCH_ZIP='jsch-0.1.44.zip'

MIME_DIR='httpcomponents-client-4.1.2'
MIME_URL='http://mirrors.enquira.co.uk/apache//httpcomponents/httpclient'
MIME_URL="${MIME_URL}/source/httpcomponents-client-4.1.2-src.tar.gz"

sudo apt-get --assume-yes install \
    maven2

# Lib directory location
LIB_DIR="`readlink -f $(dirname "$0")`/lib/"

# Build httpmime
wget -O - "${MIME_URL}" | tar --directory='/tmp/' -xz
cd "/tmp/${MIME_DIR}"
mvn package
mv httpmime/target/httpmime-4.1.2.jar "${LIB_DIR}/httpmime-4.1.2.jar"
cd ..
rm -fr "./${MIME_DIR}"

# Build JSch
cd /tmp
wget "${JSCH_URL}"
unzip "./${JSCH_ZIP}"
rm "./${JSCH_ZIP}"
cd "./${JSCH_DIR}"
ant dist
mv dist/lib/jsch-*.jar "${LIB_DIR}/jsch-0.1.44.jar"
cd ..
rm -fr "./${JSCH_DIR}"
