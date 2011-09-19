#!/usr/bin/env python2.6
from __future__ import division
from __future__ import print_function
import os

def abspath(source_rel_path, target_rel_path):
    return os.path.abspath(os.path.join(os.path.dirname(
        os.path.abspath(source_rel_path)), target_rel_path))

def get_input(prompt, strip=True):
    while True:
        responce = raw_input('%s: ' % prompt)
        if strip:
            responce = responce.strip()
        if responce:
            return responce
        print('Nothing entered, please retry.')
