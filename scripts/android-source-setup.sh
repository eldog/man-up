#!/bin/bash
set -o errexit
set -o nounset

REPO_MD5='bbf05a064c4d184550d71595a662e098'
REPO_PATH="$HOME/Dropbox/bin/repo"
REPO_URL='https://android.git.kernel.org/repo'

SOURCE_PATH="$HOME/android-source"

# Setup repo
wget -O "$REPO_PATH" "$REPO_URL"
if [ "`openssl md5 "$REPO_PATH" | cut -d ' ' -f 2`" != "$REPO_MD5" ]; then
    echo 'Repo invalid! Aborting.'
    exit 1
fi
chmod a+x "$REPO_PATH"

# Download source
mkdir "$SOURCE_PATH"
cd "$SOURCE_PATH"
repo init -u git://android.git.kernel.org/platform/manifest.git
repo sync
