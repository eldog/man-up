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

def _parse_argv(argv):
    p = ArgumentParser(version='%prog ver. 0.1 alpha 2011')

    p.add_argument('-a', '--android-address',
        help='The MAC address of the Bluetooth adapter of the Android device running SwipeUp')

    p.add_argument('-d', '--dummy-reader',
        dest='dummy',
        action='store_true',
        help='use a dummy card reader instead of looking for a real one')

    p.add_argument('-g', '--gui',
        action="store_true",
        help='use GUI instead of CLI')

    p.add_argument('-l', '--ldap-host',
        required=True,
        help='the address of the ldap host')

    p.add_argument('-L', '--ldap-port',
        required=True,
        type=int,
        help='the port to contact the ldap host on')

    p.add_argument('-w', '--webservice-host',
        help='the address of the web service host')

    p.add_argument('-W', '--webservice-port',
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

    personal_id_queue = Queue.Queue()
    # If Sign Up isn't used, this queue is filled but never emptied: memory leak!
    signup_queue = Queue.Queue()
    manup_queue = Queue.Queue()

    card_reader_client = CardReadClient(
        outgoing_queues=(personal_id_queue, manup_queue, signup_queue),
        dummy=args.dummy)
    card_reader_client.start()

    if args.android_address is not None:
        from swipeup.clients.signup import SignUpClient
        signup_client = SignUpClient(
            args.android_address,
            signup_queue)
        signup_client.start()

    if args.gui:
        from swipeup.interfaces.gui import SwipeUpGui
        interface = SwipeUpGui()
    else:
        from swipeup.interfaces.cli import SwipeUpCli
        interface = SwipeUpCli(card_reader_client)

    uom_ldap_client = UomLdapClient(
        args.ldap_host,
        args.ldap_port,
        personal_id_queue,
        outgoing_queues=(signup_queue, manup_queue),
        callbacks=(interface.json_update,))
    uom_ldap_client.start()

    if args.webservice_host is not None:
        manup_client = ManUpClient(
            args.webservice_host,
            args.webservice_port,
            manup_queue)
        manup_client.start()

    interface.mainloop()

if __name__ == '__main__':
    exit(main())
