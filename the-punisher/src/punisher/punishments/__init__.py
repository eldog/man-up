#!/usr/bin/env python2.6
from __future__ import division
from __future__ import print_function
from functools import wraps
from types import TypeType
import sys

def safety(func):
    @wraps(func)
    def wrapper(self, *args, **kwargs):
        if self._punisher.safe_mode:
            print('Safe mode is suppressing call to %s.' % func.__name__,
               file=sys.stderr)
        else:
            return func(self, *args, **kwargs)
    return wrapper

class Punishment(object):
    enabled = False
    requires_configuration = False
    
    def __init__(self, punisher):
        self._punisher = punisher
    
    def __str__(self):
        return self.__class__.__name__
        
    def configure(self, settings):
        raise NotImplementedError
    
    @safety
    def punish(self):
        raise Exception('punishment not implemented')


# ==============================================================================
# = Installed punishments                                                      =
# ==============================================================================
# from punisher.punishments.olive import DeleteHomeDirPunishment
from punisher.punishments.olive import HurtfulTwitterPost
# ==============================================================================

def _build_punishment_set():
    punishements = set()
    for obj in globals().itervalues():
        if obj is not Punishment and isinstance(obj, TypeType) \
        and issubclass(obj, Punishment):
            punishements.add(obj)
    return punishements
PUNISHMENTS = _build_punishment_set()
