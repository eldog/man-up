from __future__ import division
from __future__ import print_function
from argparse import ArgumentParser
import asyncore
import os
import Queue
import urllib
import urllib2
import socket
import sys
import threading

def abspath(path):
    return os.path.abspath(os.path.join(os.path.dirname(__file__), path))

def _config_path():
    lib_path = abspath('../lib')
    if lib_path not in sys.path:
        sys.path.append(lib_path)
_config_path()

import ldap
import msr605

_API = 'swipe/'
_QUERY = 'uman_mag_stripe'

MAG_STRIP_FILE = 'mag_strip_set.pickle'

def _parse_argv(argv):
    p = ArgumentParser(version='%prog ver. 0.1 alpha 2011')

    p.add_argument('-a', '--android-host',
        dest='android_host',
        required=True,
        help='the address of the Android device running Autograph')

    p.add_argument('-A', '--android-port',
        dest='android_port',
        required=True,
        type=int,
        help='the port on which to contact the Android device on')

    p.add_argument('-d', '--dummy-reader',
        dest='dummy',
        action='store_true',
        help='use a dummy card reader instead of looking for a real one')

    p.add_argument('-g', '--gui',
        action="store_true",
        help='use GUI instead of CLI')

    p.add_argument('-l', '--ldap-host',
        dest='ldap_host',
        required=True,
        help='the address of the ldap host')

    p.add_argument('-L', '--ldap-port',
        dest='ldap_port',
        required=True,
        type=int,
        help='the port to contact the ldap host on')

    p.add_argument('-w', '--webservice-host',
        dest='webservice_host',
        required=True,
        help='the address of the web service host')

    p.add_argument('-W', '--webservice-port',
        dest='webservice_port',
        required=True,
        type=int,
        help='the port on which to contact the web service host')

    return p.parse_args(args=argv[1:])

class Ldapper(object):
    def __init__(self, host, port):
        self._host = host
        self._port = port

    def lookup(self, uman_mag_stripe):
        ldap_conn = ldap.initialize('ldap://%s:%s' % (self._host, self._port))
        try:
            ldap_result = ldap_conn.search_s('', ldap.SCOPE_SUBTREE,
                '(umanMagStripe=%s)' % uman_mag_stripe)
        except ldap.SERVER_DOWN:
            print('error: unable to connect to LDAP server', file=sys.stderr)
            return None
        if ldap_result:
            ldap_result = ldap_result[0][1]
        else:
            ldap_result = None
        return ldap_result


class ThreadLdapper(threading.Thread):
    def __init__(self, ldapper, mag_stripe_queue, response_queue):
        threading.Thread.__init__(self)
        self.ldapper = ldapper
        self.mag_stripe_queue = mag_stripe_queue
        self.response_queue = response_queue

    def run(self):
        while True:
            mag_stripe = self.mag_stripe_queue.get()
            response = self.ldapper.lookup(mag_stripe)
            if response:
                self.response_queue.put(response)
                self.mag_stripe_queue.task_done()


class CardReader(object):
    def __init__(self, dummy=False):
        self._get_reader = msr605.from_dummy if dummy else msr605.from_serial

    def read_card(self):
        with self._get_reader() as msr605:
            print('Ready to swipe up')
            try:
                mag_stripe = msr605.read()[1].fields[0]
            except IOError:
                print('Failed to read try again', file=sys.stderr)
                return None
            print('mag stripe was %s' % mag_stripe)
            return mag_stripe


class ThreadCardReader(threading.Thread):
    def __init__(self, card_reader, mag_stripe_queue, android_queue):
        threading.Thread.__init__(self)
        self.card_reader = card_reader
        self.mag_stripe_queue = mag_stripe_queue
        self.android_queue = android_queue

    def run(self):
        while True:
            mag_stripe = self.card_reader.read_card()
            if mag_stripe:
                self.mag_stripe_queue.put(mag_stripe)
                self.android_queue.put(mag_stripe)


class AndroidClient(asyncore.dispatcher):
    def __init__(self, host, port, message):
        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connect((host, port))
        self.buffer = '%s\n' % message

    def handle_connect(self):
        pass

    def handle_close(self):
        print('handling close')
        self.close()

    def handle_read(self):
        print('handling')
        print(self.recv(8192))

    def writable(self):
        return (len(self.buffer) > 0)

    def handle_write(self):
        sent = self.send(self.buffer)
        self.buffer = self.buffer[sent:]

    def handle_error(self, error):
        print(error, file=sys.stderr)
        raise Exception

class ThreadAndroid(threading.Thread):
    def __init__(self, host, port, android_queue):
        threading.Thread.__init__(self)
        self._host = host
        self._port = port
        self.android_queue = android_queue

    def run(self):
        while True:
            message = self.android_queue.get()
            android_client = AndroidClient(self._host, self._port, message)
            try:
                asyncore.loop()
            except Exception:
                print('Error in android connection', file=sys.stderr)
            self.android_queue.task_done()

class ThreadResponse(threading.Thread):
    def __init__(self, response_queue, *callbacks):
        threading.Thread.__init__(self)
        self.response_queue = response_queue
        self.callbacks = callbacks

    def run(self):
        while True:
            response = self.response_queue.get()
            for callback in self.callbacks:
                callback(response)
            self.response_queue.task_done()


class ServerClient(object):
    def __init__(self, host, port):
        self._host = host
        self._port = port

    def send(self, json_response):
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
                'http://%s:%d/%s' % (self._host, self._port, _API),
                data=params)
        except (urllib2.HTTPError, urllib2.URLError) as e:
            print('Error occured connecting to appengine: %s' % e, file=sys.stderr)
        else:
            print('Sent to appengine')


def main(argv=None):
    if argv is None:
        argv = sys.argv
    args = _parse_argv(argv)

    mag_stripe_queue = Queue.Queue()
    android_queue = Queue.Queue()
    response_queue = Queue.Queue()

    ldapper = Ldapper(args.ldap_host, args.ldap_port)
    ldapper_thread = ThreadLdapper(ldapper, mag_stripe_queue,
                                   response_queue)
    ldapper_thread.setDaemon(True)
    ldapper_thread.start()

    card_thread = ThreadCardReader(CardReader(args.dummy),
        mag_stripe_queue, android_queue)
    card_thread.setDaemon(True)
    card_thread.start()

    client_thread = ThreadAndroid(args.android_host, args.android_port,
        android_queue)
    client_thread.setDaemon(True)
    client_thread.start()


    if args.gui:
        from swipeup.interfaces.gui import SwipeUpGui
        interface = SwipeUpGui()
    else:
        from swipeup.interfaces.cli import SwipeUpCli
        interface = SwipeUpCli(card_thread)

    server_client = ServerClient(args.webservice_host, args.webservice_port)

    response_thread = ThreadResponse(response_queue,
        interface.json_update, server_client.send)
    response_thread.setDaemon(True)
    response_thread.start()

    interface.mainloop()

if __name__ == '__main__':
    exit(main())
