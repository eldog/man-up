from __future__ import division
from __future__ import print_function
import json
import logging
import threading

import bluetooth

_logger = logging.getLogger('SignUpClient')

SWIPE_UP_UUID = '28adccbc-41a3-4ffd-924d-1c6a70d70b4e'

class SignUpClient(threading.Thread):
    def __init__(self, address, person_id_queue):
        super(SignUpClient, self).__init__()
        self.daemon = True
        self._address = address
        self._person_id_queue = person_id_queue

    def run(self):
        while True:
            person_id = self._person_id_queue.get()
            message = json.dumps({'umanPersonID': person_id}) + '\n'
            try:
                service_matches = bluetooth.find_service(uuid=SWIPE_UP_UUID, address=self._address)
                first_match = service_matches[0]
                socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
                socket.connect((first_match["host"], first_match["port"]))
                socket.send(message)
                socket.close()
            except Exception as e:
                _logger.error("Failed to request signature from SwipeUp: %s", e)
            else:
                _logger.debug("Request for signature made to SwipeUp: %s", person_id)
            self._person_id_queue.task_done()

