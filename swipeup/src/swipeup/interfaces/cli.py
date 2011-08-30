from __future__ import division
from __future__ import print_function

class SwipeUpCli(object):
    def __init__(self, thread):
        self._wait_for = thread

    def json_update(self, json):
        name = json['cn'][0]
        email = json['mail'][0]
        student_id = json['umanPersonID'][0]
        print('Name: %s\nEmail: %s\nStudent ID: %s' % (name, email, student_id))

    def mainloop(self):
        self._wait_for.join()
        print('Exiting')
