from argparse import ArgumentParser
import subprocess
import sys

SWIFT_NAME = 'swift'
WHICH_PATH = '/bin/which'

class ExecutableFinder:
    def __init__(self, which_path):
        self._path = which_path

    def find(self, executable_name):
        try:
            out = subprocess.check_output(
                args=(self._path, executable_name))
        except subprocess.CalledProcessError:
            return None
        else:
            return out.strip().decode('ascii')


class Swift:
    def __init__(self, swift_path, pulse_audio_dsp_path):
        self._swift = swift_path
        self._padsp = pulse_audio_dsp_path

    def say(self, text):
        subprocess.check_call(args=(self._padsp, self._swift, '-m', 'text', text))


def main(argv=None):
    if argv is None:
        argv = sys.argv

    ap = ArgumentParser()
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

    swift = Swift(swift_path, padsp_path)
    swift.say("Hello world!")

if __name__ == '__main__':
    exit(main())
