from argparse import ArgumentParser
from array import array
from collections import namedtuple
import logging
from struct import pack
from time import sleep
import os
import queue
import re
import subprocess
import sys

import cherrypy

from threadpool import StoppableThread, ThreadPool

logger = logging.getLogger(name="tts")

def relpath(path):
    return os.path.join(os.path.dirname(__file__), path)

class ExecutableFinder:
    def __init__(self, which_path):
        self._which_path = which_path

    def find(self, executable_name):
        args = (self._which_path, executable_name)
        return subprocess.check_output(args)[:-1].decode('us-ascii')

# =============================================================================
# = Text-to-Speech                                                            =
# =============================================================================

class Swift:
    def __init__(self, swift_path, padsp_path, threshold=0.2):
        self._swift_path = swift_path
        self._padsp_path = padsp_path
        self._threshold = threshold

    def tts(self, ssml):
        subprocess.check_call((self._padsp_path, self._swift_path, ssml))

    def tts_file(self, ssml, output_path, volume=100):
        bits_per_sample = 16
        bytes_per_sample = bits_per_sample // 8
        num_channels = 1
        sample_rate = 16000

        samples = array('h', subprocess.check_output((
            self._swift_path,
            '-o', '-',
            '-p',
                'audio/channels=%s,'
                'audio/deadair=1000,'
                'audio/encoding=pcm%s,'
                'audio/output-format=raw,'
                'audio/sampling-rate=%s,'
                'audio/volume=%s'
                    % (num_channels, bits_per_sample, sample_rate, volume),
            ssml)))

        num_samples = len(samples)
        num_blocks = num_samples // num_channels
        subchunk_2_size = num_blocks * num_channels * bytes_per_sample

        with open(output_path, 'wb') as f:
            f.write(b'RIFF') # Chunk ID

            # Bytes to follow
            f.write(pack('<I', 36 + subchunk_2_size))

            f.write(b'WAVEfmt ') # Sub chunk ID

            f.write(pack('<I', 16)) # Byte to follow in sub chunk

            f.write(pack('<H', 1)) # Audio format: PCM

            f.write(pack('<H', num_channels)) # Channels: Mono

            f.write(pack('<I', sample_rate)) # Sample rate (Hz)

            f.write(pack('<I', sample_rate * num_channels * bytes_per_sample))

            f.write(pack('<H', num_channels * bytes_per_sample))

            f.write(pack('<H', bits_per_sample)) # Bits per sample

            f.write(b'data') # Subchunk ID

            # Subchunk 2 size
            f.write(pack('<I', subchunk_2_size))

            # Data
            samples.tofile(f)

        start_sample = 0
        previous_sample = 0
        threshold = (2 ** 16 / 2) * self._threshold
        for i, sample in enumerate(samples):
            if previous_sample < 0 and sample >= 0 \
            or previous_sample >= 0 and sample < 0:
                start_sample = i
            if abs(sample) > threshold:
                break
            previous_sample = sample

        return -start_sample / sample_rate


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
Bb4 = 466.16
B4 = 493.88
C5 = 523.25
Db5 = 554.37
D5 = 587.33
Eb5 = 622.25
E5 = 659.25
F5 = 698.46
Gb5 = 739.99
G5 = 783.99
Ab5 = 830.61
A5 = 880.00
Bb5 = 932.33
B5 = 987.77
C6 = 1046.50
Db6 = 1108.73
D6 = 1174.66
Eb6 = 1244.51
E6 = 1318.51
F6 = 1396.91
Gb6 = 1479.98
G6 = 1567.98
Ab6 = 1661.22
A6 = 1760.00
Bb6 = 1864.66
B6 = 1975.53
C7 = 2093.00
Db7 = 2217.46
D7 = 2349.32
Eb7 = 2489.02
E7 = 2637.02
F7 = 2793.83
Gb7 = 2959.96
G7 = 3135.96
Ab7 = 3322.44
A7 = 3520.00
Bb7 = 3729.31
B7 = 3951.07
C8 = 4186.01
Db8 = 4434.92
D8 = 4698.64
Eb8 = 4978.03
REST = 0.0

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

note_matcher = re.compile(
    '\A([abcdefg]b?[012345678]|r)(?:_(\d+)(?:/(\d+))?)?\Z',
    re.IGNORECASE).match

def parse_melody(notes):
    g = globals()
    ns = []
    for note in notes.split():
        match = note_matcher(note)
        if not match:
            raise ValueError('Invalid note %r.' % note)
        pitch = match.group(1)
        if pitch == 'r':
            pitch = REST
        else:
            pitch = g[pitch[0].upper() + pitch[1:]]
        n = match.group(2)
        n = int(n) if n else 1
        d = match.group(3)
        d = int(d) if d else 1
        ns.append((pitch, n / d))
    return ns

# =============================================================================

SmsMessage = namedtuple('SmsMessage', ('msg_id', 'number', 'message'))

class SmsHandler(StoppableThread):

    dictionary_path = None
    play_path = None
    sox_path = None

    def __init__(self, sms_queue, swift, backing_sample, melody, bpm=100,
        thread_pool=10):
        super().__init__()
        self.daemon = True
        self._sms_queue = sms_queue
        self._melody = parse_melody(melody)
        self._spb = 60 / bpm
        self._thread_pool = thread_pool
        self._speech = []
        self._rap_mode = False
        self._rap_path = None
        self._swift = swift
        self._backing_sample = backing_sample

    def run(self):
        self._real_words = set()
        with open(self.dictionary_path) as f:
            for w in f:
                w = w.strip()
                if w.isalpha():
                    self._real_words.add(w.lower())

        while not self._stop_event.is_set():
            try:
                msg_id, number, message = self._sms_queue.get_nowait()
            except queue.Empty:
                sleep(0.1)
                continue
            if number == 'admin':
                if message == 'compile':
                    print('Entering rap mode, compiling rap.')
                    self._rap_mode = True
                    self._rap_path = self.render_rap(msg_id, self._speech)
                    print('Rap compilied.')
                elif message == 'rap' and self._rap_path:
                    subprocess.check_call((self.play_path, '-q',
                        self._rap_path))
                elif message == 'speech':
                    self._rap_mode = False
            elif not self._rap_mode:
                self.render_speech(message)
                pass

            self._sms_queue.task_done()

    def render_speech(self, message):
        if 'cake' in message:
            message = 'To who ever tried to make me say a Portal reference ' \
                'think of some thing better'
        words = self.clean_message(message)
        if words:
            self._speech.extend(words)
            logger.error(str(len(self._speech)))
            self._swift.tts('<s><prosody rate="slow">%s</prosody></s>'
                % ' '.join(words[:20]))

    def render_rap(self, msg_id, words):
        # Make the length of words fit the melody
        notes = sum(1 for pitch, beats in self._melody if pitch != REST)
        diff = notes - len(words)
        if diff < 0:
            words = words[:diff]
        else:
            words = words + ['la'] * diff

        delay = 0
        offsets = {}
        word_index = 0
        word_count = len(words)
        word_delays = []
        word_paths = []

        pool = ThreadPool(min(word_count, self._thread_pool))

        for pitch, beats in self._melody:
            duration = beats * self._spb

            if pitch != REST:
                word = words[word_index]
                word_delays.append(delay)
                word_path = '/tmp/%s-%s.wav' % (msg_id, word_index)
                word_paths.append(word_path)
                ssml = '<s><prosody pitch="%sHz" range="x-low">%s</prosody></s>' \
                    % (pitch, word)
                def task(word_id, ssml, word_path):
                    offsets[word_id] = self._swift.tts_file(ssml, word_path)
                pool.queue_task(task, (word_index, ssml, word_path))
                word_index += 1

            delay += duration

            if word_index == word_count:
                # Break here, rather than inside the if statement above, so that
                # that delay is updated and equals the duration of the rap.
                break

        pool.join_all()

        if not word_index:
            # Didn't render any words!
            return

        # Mix the rap and the backing track
        mix_path = '/tmp/%s-mix.wav' % msg_id
        sox_args = [self.sox_path, '-M'] + word_paths \
            + [self._backing_sample, mix_path, 'delay'] \
            + [str(delay + offsets[i]) for i, delay in enumerate(word_delays)] \
            + ['remix',
                ','.join(str(channel) for channel in range(1, word_count + 2)),
                'norm']
        print(' '.join(sox_args))
        subprocess.check_call(sox_args)

        return mix_path

    def clean_message(self, message):
        words = []
        for word in message.split():
            chars = [c for c in word if c.isalpha()]
            if chars:
                word = ''.join(chars).lower()
                if word in self._real_words:
                    words.append(word)
        return words


class SmsServer:
    def __init__(self, hostname, port, incoming_sms_queue, reply):
        self._hostname = hostname
        self._port = port
        self._sms_queue = incoming_sms_queue
        self._reply = reply
        self._msg_id = 0

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def index(self, message=None, number=None):
        self._sms_queue.put(SmsMessage(self._msg_id, number, message))
        self._msg_id += 1
        return [{'number' : number, 'message' : self._reply}]

    def mainloop(self):
        cherrypy.config.update({
            'server.socket_host': self._hostname,
            'server.socket_port': self._port})
        cherrypy.quickstart(self)


class DummySmsServer:
    def __init__(self, _hostname, incoming_sms_queue, reply, message='',
            loop=True):
        self._sms_queue = incoming_sms_queue
        self._msg = message
        self._loop = loop
        self._reply = reply
        self._msg_id = 0

    def mainloop(self):
        def recieve_message():
            if self._msg:
                input('Press return to send message %r' % self._msg)
                return self._msg
            else:
                return input('Enter message: ')

        def queue_message(message):
            self._sms_queue.put(SmsMessage(self._msg_id, '+447555555555',
                message))
            self._msg_id += 1

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
    ap.add_argument('-b', '--bpm',
        type=float,
        required=True,
        help='The BPM of the rap.')
    ap.add_argument('-B', '--backing-sample',
        required=True,
        help='The path of the backing sample.')
    ap.add_argument('-d', '--dummy-sms-server',
        action='store_true',
        help='Use the dummy SMS server.')
    ap.add_argument('-D', '--dummy-message',
        default='',
        help='A message to be used by the dummy server.')
    ap.add_argument('-H', '--hostname',
        required=True,
        help='The host name of the SMS server.')
    ap.add_argument('-m', '--melody',
        required=True,
        help='The melody to rap to.')
    ap.add_argument('-p', '--port',
        required=True,
        type=int,
        help='The port of the server.')
    ap.add_argument('-r', '--reply',
        required=True,
        help='The reply message.')
    ap.add_argument('-t', '--thread-pool',
        type=int,
        default=10,
        help='The size of the thread pool used when rendering the rap.')
    ap.add_argument('-T', '--threshold',
        type=float,
        default=0.2,
        help='The % volume at which a word sample will start.')
    ap.add_argument('-w', '--which-path',
        default='/usr/bin/which',
        help='The path of the which executable.')
    ap.add_argument('-W', '--words',
        default='/etc/dictionaries-common/words',
        help='The list of acceptable words')
    ap.add_argument('--dummy-loop',
        action='store_true',
        help='Loop the dummy server')
    args = ap.parse_args(argv[1:])

    finder = ExecutableFinder(args.which_path)
    SmsHandler.dictionary_path = args.words
    SmsHandler.play_path = finder.find('play')
    SmsHandler.sox_path = finder.find('sox')

    incoming_sms = queue.Queue()
    swift = Swift(finder.find('swift'), finder.find('padsp'))

    sms_handler = SmsHandler(incoming_sms, swift, args.backing_sample,
        melody=args.melody, bpm=args.bpm, thread_pool=args.thread_pool)
    sms_handler.start();

    if args.dummy_sms_server:
        def server(*passed_args, **kwargs):
            return DummySmsServer(*passed_args,
                message=args.dummy_message, loop=args.dummy_loop,
                ** kwargs)
    else:
        server = SmsServer

    server(
        args.hostname,
        args.port,
        incoming_sms,
        args.reply).mainloop();

    sms_handler.join()

    print('Exiting.')

if __name__ == '__main__':
    exit(main())
