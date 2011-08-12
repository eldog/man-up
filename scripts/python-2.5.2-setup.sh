#!/bin/bash
# Install Python 2.5.2 on Natty. This is the version used by Appegine.
set -o errexit
set -o nounset

PYTHON_DIR='Python-2.5.2'
PYTHON_URL='http://www.python.org/ftp/python/2.5.2/Python-2.5.2.tar.bz2'

SSL_DIR='ssl-1.15'
SSL_URL='http://pypi.python.org/packages/source/s/ssl/ssl-1.15.tar.gz'

PIL_DIR='Imaging-1.1.7'
PIL_URL='http://effbot.org/downloads/Imaging-1.1.7.tar.gz'

# Required packages.
sudo apt-get --assume-yes install \
    libsqlite3-dev \
    libssl-dev

# Hack for old Ubuntu naming conventions.
cd '/lib'
sudo ln -s 'x86_64-linux-gnu/libz.so.1' 'libz.so'
cd '/usr/lib'
sudo ln -s 'x86_64-linux-gnu/libsqlite3.so' 'libsqlite3.so'

# Download source.
cd '/tmp'
wget -O - "$PYTHON_URL" | tar -xj
cd "$PYTHON_DIR"

# Build & install.
./configure
make
sudo make install
sudo rm '/usr/local/bin/python'

# PIL
cd /tmp
wget -O - "$PIL_URL" | tar -xz
cd "$PIL_DIR"
python2.5 setup.py build_ext --include-dirs='../Python-2.5.2/Include'
python2.5 setup.py build
sudo python2.5 setup.py install

# SSL
cd /tmp
wget -O - "$SSL_URL" | tar -xz
cd "$SSL_DIR"
python2.5 setup.py build
sudo python2.5 setup.py install

# Clean up.
sudo rm '/lib/libz.so' '/usr/lib/libsqlite3.so'
rm -fr "/tmp/$PYTHON_DIR" "/tmp/$PIL_DIR" "/tmp/$SSL_DIR"

