from __future__ import division
from __future__ import print_function
import asyncore
import logging
import socket
import threading

_logger = logging.getLogger('SignUpClient')

class AndroidClient(asyncore.dispatcher):
    def __init__(self, host, port, message):
        asyncore.dispatcher.__init__(self)
        # I don't believe this is correct/good.
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connect((host, port))
        self.buffer = '%s\n' % message

    def handle_connect(self):
        pass

    def handle_close(self):
        self.close()

    def handle_read(self):
        print(self.recv(8192))

    def writable(self):
        return (len(self.buffer) > 0)

    def handle_write(self):
        sent = self.send(self.buffer)
        self.buffer = self.buffer[sent:]

    def handle_error(self):
        pass


class SignUpClient(threading.Thread):
    def __init__(self, host, port, signup_queue):
        super(SignUpClient, self).__init__()
        self.daemon = True

        self._host = host
        self._port = port
        self._signup_queue = signup_queue

    def run(self):
        while True:
            message = self._signup_queue.get()
            AndroidClient(self._host, self._port, message)
            try:
                asyncore.loop()
            except Exception as e:
                _logger.error('Failed to connect to SignUp: %s', e)
            self._signup_queue.task_done()

