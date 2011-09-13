from argparse import ArgumentParser
from collections import namedtuple
import queue
import subprocess
import sys
import threading

from ssml import SsmlDocument

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
        self._swift = None
        self._swift_path = swift_path
        self._padsp_path = pulse_audio_dsp_path

    def __enter__(self):
        if self._swift is None:
            self._swift = subprocess.Popen(
                args=(self._padsp_path, self._swift_path, '-f', '-'),
                stdin=subprocess.PIPE)
        return self

    def __exit__(self, type, value, traceback):
        self._swift.terminate()
        self._swift = None

    def speak(self, ssml):
        self._swift.stdin.write(ssml)
        self._swift.stdin.write(b'\n\n')
        self._swift.stdin.flush()


class StoppableThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._stop_event = threading.Event()

    def join(self):
        self._stop_event.set()
        super().join()


class SmsHandler(StoppableThread):
    def __init__(self, swift_path, pulse_audio_dsp_path, incoming_sms_queue):
        super().__init__()
        self.daemon = True
        self._swift_path = swift_path
        self._padsp_path = pulse_audio_dsp_path
        self._queue = incoming_sms_queue

    def run(self):
        with Swift(self._swift_path, self._padsp_path) as swift:
            while not self._stop_event.is_set():

                try:
                    message = self._queue.get_nowait()
                except queue.Empty:
                    continue

                ssml = SsmlDocument()
                ssml.append_sentance(message.message)
                swift.speak(ssml.toxml(encoding='us-ascii'))
                self._queue.task_done()


class DummySmsSupplier:
    def __init__(self, queue):
        self._queue = queue

    def mainloop(self):
        while True:
            message = input('Enter message: ')
            if not message:
                break
            self._queue.put(SmsMessage('+447555555555', message))


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
        print('Could not find the PulseAudio DSP (padsp) emulation executable',
            file=sys.stderr)
        return 1

    swift_path = finder.find(SWIFT_NAME)
    if not swift_path:
        print('Could not find the swift executable.', file=sys.stderr)
        return 1

    incoming_sms = queue.Queue()

    sms_handler = SmsHandler(swift_path, padsp_path, incoming_sms)
    sms_handler.start();

    if args.dummy_sms_supplier:
        sms_supplier = DummySmsSupplier(incoming_sms)

    sms_supplier.mainloop();

    sms_handler.join()

    print('Exiting.')

if __name__ == '__main__':
    exit(main())
