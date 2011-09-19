from __future__ import division
from __future__ import print_function
import json
import logging
import threading
import time

import bluetooth

_logger = logging.getLogger('SignUpClient')

SWIPE_UP_UUID = '28adccbc-41a3-4ffd-924d-1c6a70d70b4e'

class SignUpClient(threading.Thread):
    def __init__(self, address, person_id_queue):
        super(SignUpClient, self).__init__()
        self.daemon = True
        self._address = address
        self._person_info_queue = person_id_queue

    def run(self):
        while True:
            info = self._person_info_queue.get()
            if isinstance(info, str):
                message = json.dumps({'umanPersonID': info})
            else:
                message = json.dumps({
                    'umanPersonID': ', '.join(info['umanPersonID']),
                    'givenName': ', '.join(info['givenName']),
                    'sn': ', '.join(info['sn']),
                    'mail': ', '.join(info['mail']),
                    'ou': ', '.join(info['ou']),
                    'employeeType': ', '.join(info['employeeType'])
                })
            _logger.info('Sending: %s', message)
            try:
                service_matches = bluetooth.find_service(uuid=SWIPE_UP_UUID, address=self._address)
                if service_matches:
                    first_match = service_matches[0]
                    if service_matches:
                        socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
                        socket.connect((first_match["host"], first_match["port"]))
                        socket.send(message + '\n')
                        socket.close()
                        time.sleep(1)
                    else:
                        _logger.error('Could not find service.')
                else:
                    _logger.error('Service not found.')
            except Exception as e:
                _logger.error("Failed to request signature from SwipeUp: %s", e)
            else:
                _logger.debug("Request for signature made to SwipeUp: %s", message)
            self._person_info_queue.task_done()

