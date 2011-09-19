#!/usr/bin/env python2.6
from __future__ import division
from __future__ import print_function
import ConfigParser
import cPickle
import datetime
import os
import random
import sys
import threading

from punisher.punishments import PUNISHMENTS, safety
import punisher.utils

SETTINGS_DIR_PATH = punisher.utils.abspath(__file__, 'settings')

class PunisherError(Exception):
    pass


class User(object):
    def __init__(self, username, settings_dir_path=None):
        if settings_dir_path is None:
            settings_dir_path = SETTINGS_DIR_PATH
        self.username = username
        self._settings_path = os.path.join(settings_dir_path, self.username)

    def settings_new(self):
        self.settings = {}
        
    def settings_load(self):
        with open(self._settings_path) as settings_file:
            self.settings = cPickle.load(settings_file)

    def settings_save(self):
        with open(self._settings_path, 'w') as settings_file:
            cPickle.dump(self.settings, settings_file) 

    def settings_delete(self):
        os.remove(self._settings_path)
    

class Punisher(object):
    safe_mode = True
    
    def __init__(self, user, punishments=None):        
        if isinstance(user, str):
            self.user = User(user)
            try:
                self.user.settings_load()
            except IOError:
                self.user.settings_new()
        elif isinstance(user, User):
            self.user = user
        else:
            raise ValueError('user must either of type str or User')

        if punishments is None:
            punishments = PUNISHMENTS
        
        self._punishments = set()
        for punishment_class in punishments:
            punishment = punishment_class(self)
            if punishment.requires_configuration:
                fullname = '%s.%s' % (punishment_class.__module__, 
                    punishment_class.__name__)                
                if fullname not in self.user.settings:
                    self.user.settings[fullname] = {}
                punishment.configure(self.user.settings[fullname])
            if punishment.enabled:
                self._punishments.add(punishment)
        self._punishments = tuple(self._punishments)
        if not self._punishments:
            raise PunisherError('no punishments')
        print('Punishments loaded: %s' % ' '.join(map(str, self._punishments)))
    
    def activate(self, punish_datetime, password):
        self._password = password
        self.punish_datetime = punish_datetime
        t = punish_datetime - datetime.datetime.now()
        seconds = (t.microseconds + (t.seconds + t.days * 24 * 3600) * 10**6) \
            / 10**6
        self._timer = threading.Timer(seconds, self.punish)
        self._timer.start()
    
    def deactivate(self, password):
        if password != self._password:
            raise PunisherError('password incorrect')
        self._timer.cancel()
        
    def punish(self):
        punishment = random.choice(self._punishments)
        if self.safe_mode:
            print('Safe mode is suppressing %s punishment' % punishment)
        else:
            punishment.punish()
        
        
def main(argv=None):
    if argv is None:
        argv = sys.argv
    return 0

if __name__ == '__main__':
    exit(main())
