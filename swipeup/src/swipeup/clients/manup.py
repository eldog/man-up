from __future__ import division
from __future__ import print_function
import logging
import threading
import urllib
import urllib2

_logger = logging.getLogger('ManUpClient')

class ManUpClient(threading.Thread):
    def __init__(self, host, port, manup_queue):
        super(ManUpClient, self).__init__()
        self.daemon = True

        self._host = host
        self._port = port
        self.manup_queue = manup_queue

    def run(self):
        while True:
            response = self.manup_queue.get()
            if isinstance(response, str):
                # Add code to add poeple with just person ID.
                pass
            else:
                self._send_json(response)
            self.manup_queue.task_done()

    def _send_json(self, json_response):
        given_name = json_response['givenName'][0]
        surname = json_response['sn'][0]
        email = json_response['mail'][0]
        student_id = json_response['umanPersonID'][0]

        params = urllib.urlencode({'given_name' : given_name,
                                   'surname' : surname,
                                   'email' : email,
                                   'student_id' : student_id})
        try:
            response = urllib2.urlopen(
                'http://%s:%d/swipe/' % (self._host, self._port),
                data=params)
        except (urllib2.HTTPError, urllib2.URLError) as e:
            _logger.error('Failed to connect to Man-UP: %s', e)
        else:
            _logger.info('Sent to Man-UP.')

