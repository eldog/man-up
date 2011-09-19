from __future__ import division
from __future__ import print_function
import logging
import threading

_logger = logging.getLogger('CardReaderClient')

import msr605

def parse_mag_stripe(mag_stripe):
    return mag_stripe[4:-1]

class CardReadClient(threading.Thread):
    def __init__(self, outgoing_queues=(), dummy=False):
        super(CardReadClient, self).__init__()
        self.daemon = True

        self._queues = outgoing_queues
        self._get_reader = msr605.from_dummy if dummy else msr605.from_serial

    def run(self):
        while True:
            person_id = self._read_card()
            if person_id:
                for queue in self._queues:
                    queue.put(person_id)

    def _read_card(self):
        with self._get_reader() as msr605:
            _logger.info('Waiting for card swipe.')
            try:
                person_id = parse_mag_stripe(msr605.read()[1].fields[0])
            except IOError:
                _logger.error('Failed to read mag stripe.')
            else:
                _logger.info('Person ID: %s', person_id)
                return person_id
