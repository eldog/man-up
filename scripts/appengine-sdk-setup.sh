#!/bin/bash
set -o errexit
set -o nounset

SDK_DIR='google_appengine'
SDK_URL='http://googleappengine.googlecode.com/files'
SDK_URL="${SDK_URL}/google_appengine_1.5.3.zip"
SDK_ZIP='google_appengine_1.5.3.zip'

cd /tmp/
wget "${SDK_URL}"
unzip "./${SDK_ZIP}"
rm -fr "./${SDK_ZIP}"
cd "./${SDK_DIR}"

# Change hash bang to '#!/usr/bin/env python2.5'
sed -i '1s/$/2.5/' dev_appserver.py

# Enable TLS in mail
sed -i '192i\      smtp.ehlo()\
      smtp.starttls()\
      smtp.ehlo()' google/appengine/api/mail_stub.py

cd ..
sudo chown -R root:root "./${SDK_DIR}"
# If exists, remove currently installed SDK.
sudo rm -fr "/opt/${SDK_DIR}"
sudo mv "./${SDK_DIR}" /opt/
