#!/bin/bash
set -o errexit
set -o nounset

SDK_DIR='google_appengine'
SDK_URL='http://googleappengine.googlecode.com/files'
SDK_URL="$SDK_URL/google_appengine_1.5.2.zip"
SDK_ZIP='google_appengine_1.5.2.zip'

cd /tmp
wget "$SDK_URL"
unzip "$SDK_ZIP" -d ~
rm -fr "$SDK_ZIP"
cd "$HOME/$SDK_DIR"

# Change hash bang to '#!/usr/bin/env python2.5'
sed -i '1s/$/2.5/' dev_appserver.py

# Enable TLS in mail
sed -i '192i\      smtp.ehlo()\
      smtp.starttls()\
      smtp.ehlo()' 'google/appengine/api/mail_stub.py'

