#!/usr/bin/env python
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
import Tkinter as tk
import threading

def abspath(path):
    return os.path.abspath(
        os.path.join(os.path.dirname(__file__), path))

def remove_values_from_list(the_list, val):
    return [value for value in the_list if value != val]

def _config_path():
    LIB_DIR = '../lib/'
    LIB32_DIR = '../lib32/'
    LIB64_DIR = '../lib64/'
    if sys.maxsize > 2**32:
        print('using 64bit libs')
        _plat_lib_path = abspath(LIB64_DIR)
        sys.path = remove_values_from_list(sys.path, abspath(LIB32_DIR))
    else:
        print('using 32bit libs')
        _plat_lib_path = abspath(LIB32_DIR)
        sys.path = remove_values_from_list(sys.path, abspath(LIB64_DIR))
    if _plat_lib_path not in sys.path:
        sys.path.append(_plat_lib_path)
    _lib_path = abspath(LIB_DIR)
    if _lib_path not in sys.path:
        sys.path.append(_lib_path)
_config_path()
del _config_path

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
        self.connect( (host, port) )
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


class _SwipeFrame(tk.Frame, object):
    def __init__(self, *args, **kwargs):
        super(_SwipeFrame, self).__init__(*args, **kwargs)

        self.button = tk.Button(self, text='exit', command=self.quit)
        self.button.grid(row=1, column=0, padx=5, pady=5, stick=tk.E)

        self.response_frame = _ResponseFrame(master=self)
        self.response_frame.grid(row=0, column=0, padx=5,
                                 pady=5, stick=tk.EW)


class _ResponseFrame(tk.Frame, object):
    def __init__(self, *args, **kwargs):
        super(_ResponseFrame, self).__init__(*args, **kwargs)

        self.name_label = tk.Label(self, text='Name:')
        self.name_label.grid(row=0, column=0, padx=5, pady=5, stick=tk.W)
        self.name = tk.StringVar(self)
        self.name_entry = tk.Entry(self, textvariable=self.name,
                                   width=50)
        self.name_entry.grid(row=0, column=1, padx=5, pady=5, stick=tk.W)

        self.email_label = tk.Label(self, text='Email:')
        self.email_label.grid(row=1, column=0, padx=5, pady=5, stick=tk.W)
        self.email = tk.StringVar(self)
        self.email_entry = tk.Entry(self, textvariable=self.email,
                                    width=50)
        self.email_entry.grid(row=1, column=1, padx=5, pady=5, stick=tk.W)

        self.student_id_label = tk.Label(self, text='Student ID:')
        self.student_id_label.grid(row=2, column=0, padx=5, pady=5,
                                   stick=tk.W)
        self.student_id = tk.StringVar(self)
        self.student_id_entry = tk.Entry(self,
                                         textvariable=self.student_id,
                                         width=50)
        self.student_id_entry.grid(row=2, column=1, padx=5, pady=5,
                                   stick=tk.W)

    def json_update(self, json_response):
        self.name.set(json_response['cn'][0])
        self.email.set(json_response['mail'][0])
        self.student_id.set(json_response['umanPersonID'][0])


class SwipeUpGUI(tk.Tk, object):
    def __init__(self, *args, **kwargs):
        super(SwipeUpGUI, self).__init__(*args, **kwargs)
        self.title('Swipe Up')
        self.columnconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)

        self.frame = _SwipeFrame(master=self)
        self.frame.grid(row=1, column=0, padx=5, pady=5)

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

    tk.NoDefaultRoot();
    swipe_gui = SwipeUpGUI()
    server_client = ServerClient(args.webservice_host,
        args.webservice_port)

    response_thread = ThreadResponse(response_queue,
        swipe_gui.frame.response_frame.json_update, server_client.send)
    response_thread.setDaemon(True)
    response_thread.start()

    swipe_gui.mainloop()

if __name__ == '__main__':
    exit(main())
