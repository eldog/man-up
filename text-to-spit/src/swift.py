from argparse import ArgumentParser
from collections import namedtuple
import queue
import subprocess
import sys
import threading

SWIFT_NAME = 'swift'
WHICH_PATH = '/bin/which'

SmsMessage = namedtuple('SmsMessage', ('number', 'message'))

class ExecutableFinder:
    def __init__(self, which_path):
        self._path = which_path

    def find(self, executable_name):
        try:
            out = subprocess.check_output(
                args=(self._path, executable_name))
        except subprocess.CalledProcessError:
            return
        return out.strip().decode('ascii')


class Swift:
    def __init__(self, swift_path, pulse_audio_dsp_path):
        self._swift = swift_path
        self._padsp = pulse_audio_dsp_path

    def say(self, text):
        subprocess.check_call(args=(self._padsp, self._swift, '-m', 'text', text))


class SwiftManager(threading.Thread):
    def __init__(self, swift_path, pulse_audio_dsp_path, incoming_sms_queue):
        super().__init__()
        self.daemon = True
        self._queue = incoming_sms_queue

        self._swift = Swift(swift_path, pulse_audio_dsp_path)

    def run(self):
        while True:
            message = self._queue.get()
            self._swift.say(message.message)
            self._queue.task_done()


class DummySmsSupplier:
    def __init__(self, queue):
        self._queue = queue

    def mainloop(self):
        try:
            while True:
                message = input('Enter message: ')
                if message:
                    self._queue.put(SmsMessage('+447555555555', message))
        except (EOFError, KeyboardInterrupt):
            return


def main(argv=None):
    if argv is None:
        argv = sys.argv

    ap = ArgumentParser()
    ap.add_argument('-d', '--dummy-sms-supplier',
        action='store_true',
        help='Use the dummy SMS supplier.')
    ap.add_argument('-w', '--which-path',
        default='/bin/which',
        help='The path of the which executable.')
    args = ap.parse_args(args=argv[1:])

    finder = ExecutableFinder(args.which_path)

    padsp_path = finder.find('padsp')
    if not padsp_path:
        print('Could not find the PulseAudio DSP (padsp) emulation executable', file=sys.stderr)
        return 1

    swift_path = finder.find(SWIFT_NAME)
    if not swift_path:
        print('Could not find the swift executable.', file=sys.stderr)
        return 1

    incoming_sms = queue.Queue()

    swift_manager = SwiftManager(swift_path, padsp_path, incoming_sms)
    swift_manager.start();

    if args.dummy_sms_supplier:
        sms_supplier = DummySmsSupplier(incoming_sms)

    sms_supplier.mainloop();

    print('Exiting.')

if __name__ == '__main__':
    exit(main())
