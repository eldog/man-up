from __future__ import division
from __future__ import print_function
import logging
import threading

_logger = logging.getLogger('CardReaderClient')

import msr605

class CardReadClient(threading.Thread):
    def __init__(self, mag_stripe_queue, signup_queue, dummy=False):
        super(CardReadClient, self).__init__()
        self.daemon = True

        self.mag_stripe_queue = mag_stripe_queue
        self.signup_queue = signup_queue
        self._get_reader = msr605.from_dummy if dummy else msr605.from_serial

    def run(self):
        while True:
            mag_stripe = self._read_card()
            if mag_stripe:
                self.mag_stripe_queue.put(mag_stripe)
                self.signup_queue.put(mag_stripe)

    def _read_card(self):
        with self._get_reader() as msr605:
            _logger.info('Waiting for card swipe.')
            try:
                mag_stripe = msr605.read()[1].fields[0]
            except IOError:
                _logger.error('Failed to read mag stripe.')
            else:
                _logger.info('Mag stripe: %s', mag_stripe)
                return mag_stripe

