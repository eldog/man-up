from argparse import ArgumentParser
from array import array
from collections import namedtuple
from struct import pack
import queue
import re
import subprocess
import sys
import threading

import cherrypy

# =============================================================================
# = Text-to-Speech                                                            =
# =============================================================================

def tts(text, output_path, volumn=100):
    sample_rate = 16000

    samples = array('h', subprocess.check_output((
        '/usr/local/bin/swift',
        '-o', '-',
        '-p',
            'audio/channels=1,'
            'audio/deadair=0,'
            'audio/encoding=pcm16,'
            'audio/output-format=raw,'
            'audio/sampling-rate=%s,'
            'audio/volume=%s'
                % (sample_rate, volumn),
        text)))

    start = -1
    for i, sample in enumerate(samples):
        if sample:
            start = i - 1
            break

    samples = samples[start:]

    with open(output_path, 'wb') as f:
        f.write(b'RIFF') # Chunk ID
        f.write(pack('<I', 72 + len(samples))) # Bytes to follow
        f.write(b'WAVEfmt ') # Sub chunk ID
        f.write(pack('<I', 16)) # Byte to follow in sub chunk
        f.write(pack('<H', 1)) # Audio format: PCM
        f.write(pack('<H', 1)) # Channels: Mono
        f.write(pack('<I', sample_rate)) # Sample rate (Hz)
        f.write(pack('<I', 128000)) # Byte rate: 1.28 MB/s
        f.write(pack('<H', 2)) # Block align
        f.write(pack('<H', 16)) # Bits per sample
        f.write(b'data') # Subchunk ID
        f.write(pack('<I', len(samples))) # Subchunk size
        samples.tofile(f)

# =============================================================================

# =============================================================================
# = Rap Creation                                                              =
# =============================================================================

C0 = 16.35
Cb0 = 17.32
D0 = 18.35
Eb0 = 19.45
E0 = 20.60
F0 = 21.83
Gb0 = 23.12
G0 = 24.50
Ab0 = 25.96
A0 = 27.50
Bb0 = 29.14
B0 = 30.87
C1 = 32.70
Db1 = 34.65
D1 = 36.71
Eb1 = 38.89
E1 = 41.20
F1 = 43.65
Gb1 = 46.25
G1 = 49.00
Ab1 = 51.91
A1 = 55.00
Bb1 = 58.27
B1 = 61.74
C2 = 65.41
Db2 = 69.30
D2 = 73.42
Eb2 = 77.78
E2 = 82.41
F2 = 87.31
Gb2 = 92.50
G2 = 98.00
Ab2 = 103.83
A2 = 110.00
Bb2 = 116.54
B2 = 123.47
C3 = 130.81
Db3 = 138.59
D3 = 146.83
Eb3 = 155.56
E3 = 164.81
F3 = 174.61
Gb3 = 185.00
G3 = 196.00
Ab3 = 207.65
A3 = 220.00
Bb3 = 233.08
B3 = 246.94
C4 = 261.63
Db4 = 277.18
D4 = 293.66
Eb4 = 311.13
E4 = 329.63
F4 = 349.23
Gb4 = 369.99
G4 = 392.00
Ab4 = 415.30
A4 = 440.00

DEFAULT = 'default'

X_SLOW = 'x-slow'
SLOW = 'slow'
MEDIUM = 'medium'
FAST = 'fast'
X_FAST = 'x-fast'

X_LOW = 'x-low'
LOW = 'low'
HIGH = 'high'
X_HIGH = 'x-high'

note_matcher = re.compile('\A([abcdefg]b?[01234])(?:_(\d+)(?:/(\d+))?)?\Z',
    re.IGNORECASE).match

def melody(notes):
    g = globals()
    ns = []
    for note in notes.split():
        match = note_matcher(note)
        if not match:
            raise ValueError('Invalid note %r.' % note)
        pitch = match.group(1)
        pitch = g[pitch[0].upper() + pitch[1:]]
        n = match.group(2)
        n = int(n) if n else 1
        d = match.group(3)
        d = int(d) if d else 1
        ns.append((pitch, n / d))
    return ns

# =============================================================================

SmsMessage = namedtuple('SmsMessage', ('number', 'message'))

class StoppableThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._stop_event = threading.Event()

    def join(self):
        self._stop_event.set()
        super().join()


class SmsHandler(StoppableThread):
    def __init__(self, incoming_sms_queue, audio_queue, bpm=100):
        super().__init__()
        self.daemon = True
        self._sms_queue = incoming_sms_queue
        self._audio_queue = audio_queue
        self._bpm = bpm

    def run(self):
        while not self._stop_event.is_set():
            try:
                message = self._sms_queue.get_nowait().message
            except queue.Empty:
                continue
            self.render_rap(message)
            self._audio_queue.put('/tmp/out.wav')
            self._sms_queue.task_done()

    def render_rap(self, message):
        message = ''.join(c for c in message if c == ' ' or c.isalpha())


        import itertools
        spb = 60 / self._bpm
        m = itertools.cycle(melody('e3 e3_1/2 g3_1/2 e3 d3 c3_2 b2 '
            'e3 e3_1/2 g3_1/2 e3 d3_1/2 c3_1/2 d3_1/2 c3_1/2 b2'))
        sox_args = ['/usr/bin/sox', '-m']
        pad = 0

        for i, word in enumerate(message.split()):
            output = '/tmp/%d.wav' % i
            p, b = next(m)

            tts('<prosody pitch="%sHz" range="x-low">%s</prosody>' % (p, word),
                output, volumn=150)

            if not i:
                sox_args.append(output)
            else:
                sox_args.append('|sox %s -p pad %s' % (output, pad))

            # update our padding
            pad += b * spb

        sox_args.append('/tmp/out.wav')
        print(i)
        if i >= 1:
            subprocess.check_call(args=sox_args)


class AudioPlayer(StoppableThread):
    def __init__(self, audio_queue):
        super().__init__()
        self.daemon = True
        self._sms_queue = audio_queue

    def run(self):
        while not self._stop_event.is_set():
            try:
                audio_path = self._sms_queue.get_nowait()
            except queue.Empty:
                continue

            self.play_audio(audio_path)
            self._sms_queue.task_done()

    def play_audio(self, path):
        subprocess.check_call(args=('/usr/bin/play', '-q', path))



class SmsServer:
    def __init__(self, hostname, incoming_sms_queue):
        self._hostname = hostname
        self._sms_queue = incoming_sms_queue

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def index(self, message=None, number=None):
        self._sms_queue.put(SmsMessage(number, message))
        return [{'number' : number, 'message' : message}]

    def mainloop(self):
        cherrypy.config.update({'server.socket_host': self._hostname})
        cherrypy.quickstart(self)


class DummySmsServer:
    def __init__(self, _hostname, incoming_sms_queue, message='', loop=True):
        self._sms_queue = incoming_sms_queue
        self._msg = message
        self._loop = loop

    def mainloop(self):
        def recieve_message():
            if self._msg:
                input('Press return to send message %r' % self._msg)
                return self._msg
            else:
                return input('Enter message: ')

        def queue_message(message):
            self._sms_queue.put(SmsMessage('+447555555555', message))

        def recieve_and_queue():
            message = recieve_message()
            if not message:
                return False
            queue_message(message)
            return True

        if self._loop:
            while recieve_and_queue():
                pass
        elif self._msg:
            queue_message(self._msg)
            self._sms_queue.join()
        else:
            recieve_and_queue()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    ap = ArgumentParser()
    ap.add_argument('-b', '--bpm', type=int,
        default=100,
        help='The BPM of the rap.')
    ap.add_argument('-D', '--dummy-message',
        default='',
        help='A message to be used by the dummy server.')
    ap.add_argument('-d', '--dummy-sms-server',
        action='store_true',
        help='Use the dummy SMS server.')
    ap.add_argument('--dummy-loop',
        action='store_true',
        help='Loop the dummy server')
    ap.add_argument('-H', '--hostname',
        required=True,
        help='The host name of the SMS server.')
    args = ap.parse_args(args=argv[1:])

    incoming_sms = queue.Queue()
    audio = queue.Queue()

    audio_player = AudioPlayer(audio)
    audio_player.start()
    sms_handler = SmsHandler(incoming_sms, audio, bpm=args.bpm)
    sms_handler.start();

    if args.dummy_sms_server:
        def server(*passed_args, **kwargs):
            return DummySmsServer(*passed_args,
                message=args.dummy_message, loop=args.dummy_loop,
                ** kwargs)
    else:
        server = SmsServer

    server(args.hostname, incoming_sms).mainloop();

    sms_handler.join()
    audio_player.join()

    print('Exiting.')

if __name__ == '__main__':
    exit(main())
