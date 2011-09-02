from __future__ import division
from __future__ import print_function
from argparse import ArgumentParser
import logging
import os
import Queue
import sys

# Ensure the external libraries are on the path.
_lib_path = os.path.join(os.path.dirname(__file__), '../lib')
if _lib_path not in sys.path:
    sys.path.append(_lib_path)
del _lib_path

from swipeup.clients.cardreader import CardReadClient
from swipeup.clients.uomldap import UomLdapClient
from swipeup.clients.manup import ManUpClient
from swipeup.clients.signup import SignUpClient

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

    p.add_argument('-V', '--verbose',
        action='store_true',
        help='increases log level')

    return p.parse_args(args=argv[1:])

def main(argv=None):
    if argv is None:
        argv = sys.argv
    args = _parse_argv(argv)

    level = logging.INFO if args.verbose else logging.ERROR
    logging.basicConfig(level=level)

    student_id_queue = Queue.Queue()
    signup_queue = Queue.Queue()
    manup_queue = Queue.Queue()

    uom_ldap_client = UomLdapClient(
        args.ldap_host,
        args.ldap_port,
        student_id_queue,
        manup_queue)
    uom_ldap_client.start()

    card_reader_client = CardReadClient(
        student_id_queue,
        signup_queue,
        dummy=args.dummy)
    card_reader_client.start()


    signup_client = SignUpClient(
        args.android_host,
        args.android_port,
        signup_queue)
    signup_client.start()

    if args.gui:
        from swipeup.interfaces.gui import SwipeUpGui
        interface = SwipeUpGui()
    else:
        from swipeup.interfaces.cli import SwipeUpCli
        interface = SwipeUpCli(card_reader_client)

    server_client = ManUpClient(
        args.webservice_host,
        args.webservice_port,
        manup_queue,
        interface.json_update)
    server_client.start()

    interface.mainloop()

if __name__ == '__main__':
    exit(main())
