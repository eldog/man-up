#!/bin/bash
set -o errexit
set -o nounset

sudo apt-get --assume-yes install \
    python-imaging-tk \
    python-setuptools

sudo easy_install http://pypi.python.org/packages/source/s/simplejson/simplejson-2.2.1.tar.gz#md5=070c6467462bd63306f1756b01df6d70

sudo easy_install http://httplib2.googlecode.com/files/httplib2-0.7.1.tar.gz

sudo easy_install https://github.com/simplegeo/python-oauth2/tarball/master

sudo easy_install http://python-twitter.googlecode.com/files/python-twitter-0.8.2.tar.gz
