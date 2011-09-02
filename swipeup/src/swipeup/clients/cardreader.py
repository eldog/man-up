from __future__ import division
from __future__ import print_function
import logging
import threading

_logger = logging.getLogger('CardReaderClient')

import msr605

def parse_mag_stripe(mag_stripe):
    return mag_stripe[4:-1]

class CardReadClient(threading.Thread):
    def __init__(self, student_id_queue, signup_queue, dummy=False):
        super(CardReadClient, self).__init__()
        self.daemon = True

        self.student_id_queue = student_id_queue
        self.signup_queue = signup_queue
        self._get_reader = msr605.from_dummy if dummy else msr605.from_serial

    def run(self):
        while True:
            student_id = self._read_card()
            if student_id:
                self.student_id_queue.put(student_id)
                self.signup_queue.put(student_id)

    def _read_card(self):
        with self._get_reader() as msr605:
            _logger.info('Waiting for card swipe.')
            try:
                student_id = parse_mag_stripe(msr605.read()[1].fields[0])
            except IOError:
                _logger.error('Failed to read mag stripe.')
            else:
                _logger.info('Student ID: %s', student_id)
                return student_id
