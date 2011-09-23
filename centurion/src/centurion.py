#!/usr/bin/env python3
from argparse import ArgumentParser
from tkinter import font
import queue
import subprocess
import sys
import threading
import tkinter

# ==============================================================================
# = Utilities                                                                  =
# ==============================================================================

class ExecutableFinder:
    def __init__(self, which_path):
        self.which_path = which_path

    def find(self, executable_name, encoding='utf-8'):
        try:
            path = subprocess.check_output((self.which_path, executable_name))
        except subprocess.CalledProcessError:
            raise OSError('Unable to find an executable named %r'
                % executable_name)
        # Strip new line.
        path = path[:-1]
        return path.decode(encoding)


class StoppableThread(threading.Thread):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._stop_event = threading.Event()

    def join(self):
        self.stop_soon()
        super().join()

    def stop_soon(self):
        self._stop_event.set()


# ==============================================================================
# = Media Controllers                                                          =
# ==============================================================================

class Player:
    def __init__(self, play_path):
        self._play = None
        self.play_path = play_path

    def play(self, audio_path, block=True):
        self.stop()
        self._play = subprocess.Popen((self.play_path, '--no-show-progress',
            audio_path))
        if block:
            self._play.wait()
            self._play = None

    def stop(self):
        if self._play is not None:
            self._play.terminate()
            self._play = None

class Spotify:
    def __init__(self, pacmd_path):
        self.pacmd_path = pacmd_path

    def _execute_pacmd(self, *args, encoding='utf-8'):
        pacmd_args = [self.pacmd_path]
        for arg in args:
            if not isinstance(arg, str):
                arg = str(arg)
            pacmd_args.append(arg)
        try:
            output = subprocess.check_output(pacmd_args)
        except subprocess.CalledProcessError as e:
            print(e, file=sys.stderr)
        else:
            return output.decode(encoding=encoding)

    def _get_sink_input_index(self):
        output = self._execute_pacmd('list-sink-inputs')
        if output:
            index = None
            for line in output.split('\n'):
                if 'index' in line:
                    index = int(line.split(' ')[-1])
                elif 'spotify' in line.lower():
                    break
            return index

    def _mute(self, mute):
        sink_input_index = self._get_sink_input_index()
        if sink_input_index is not None:
            self._execute_pacmd('set-sink-input-mute', sink_input_index,
                int(mute))

    def mute(self):
        self._mute(True)

    def unmute(self):
        self._mute(False)


class MediaController(StoppableThread):
    def __init__(self, player, spotify=None, timeout_seconds=0.1):
        super().__init__()
        self.daemon = True
        self._player = player
        self._spotify = spotify
        self._timeout = timeout_seconds
        self._audio_queue = queue.Queue()

    def _play(self, audio_path):
        if self._spotify:
            self._spotify.mute()

        self._player.play(audio_path)

        if self._spotify:
            self._spotify.unmute()

    def run(self):
        while not self._stop_event.is_set():
            try:
                audio_path = self._audio_queue.get(timeout=self._timeout)
            except queue.Empty:
                continue
            self._play(audio_path)
            self._audio_queue.task_done()

    def play(self, audio_path, block=True):
        if block:
            self._audio_queue.join()
            self._play(audio_path)
        else:
            self._audio_queue.put(audio_path)

    def stop_soon(self):
        self._player.stop()
        self._spotify.unmute()
        super().stop_soon()


# ==============================================================================
# = GUI                                                                        =
# ==============================================================================

class CenturionGui(tkinter.Tk):
    def __init__(self,
            shot_count=100,
            shot_gap_seconds=60,
            player=None,
            drink_sample_paths=None,
            start_audio_path=None,
            end_audio_path=None,
            large_font_size=300,
            small_font_size=50):
        super().__init__()
        self._shots_left = shot_count
        self._shot_gap = shot_gap_seconds * 1000
        self._drink_sample_paths = drink_sample_paths
        self._player = player
        self._start_audio_path = start_audio_path
        self._end_audio_path = end_audio_path
        self.bind('<Escape>', lambda e: self.destroy())
        self.bind('<F1>', lambda e: self._on_start())
        self.config(background='black')
        self.title('Centurion')
        self.columnconfigure(0, weight=1)
        self.geometry('%dx%d'
            % (self.winfo_screenwidth(), self.winfo_screenheight()))
        self.rowconfigure(0, weight=1)
        self.wm_attributes('-topmost', 1)
        self._fnt_large = font.Font(root=self, size=large_font_size)
        self._fnt_small = font.Font(root=self, size=small_font_size)
        f = tkinter.Frame(background=self.cget('background'), master=self)
        f.grid(row=0, column=0)
        l = tkinter.Label(
            background=self.cget('background'),
            font=self._fnt_small,
            foreground='green',
            master=f,
            text='Shots Remaining:')
        l.grid(row=0, column=0, padx=5, pady=5)
        self._lbl_remaining = tkinter.Label(
            background=self.cget('background'),
            font=self._fnt_large,
            foreground='green',
            master=f,
            text=str(self._shots_left))
        self._lbl_remaining.grid(row=1, column=0, padx=5, pady=5)

    def _on_start(self):
        self.unbind('<F1>')
        if self._start_audio_path:
            self._play_audio(self._start_audio_path)
        self._on_drink()

    def _on_drink(self):
        self.deiconify()
        self._shots_left -= 1
        self._lbl_remaining.config(text=str(self._shots_left))
        self.update()
        if self._drink_sample_paths:
            drink_sample_path = self._drink_sample_paths[self._shots_left
                % len(self._drink_sample_paths)]
            self._play_audio(drink_sample_path, block=False)

        if self._shots_left > 0:
            self.after(3000, self.withdraw)
            self.after(self._shot_gap, self._on_drink)
        else:
            self._on_end()

    def _on_end(self):
        if self._end_audio_path:
            self._play_audio(self._end_audio_path)

    def _play_audio(self, audio_path, block=True):
        if self._player:
            self._player.play(audio_path, block=block)


def main(argv=None):
    if argv is None:
        argv = sys.argv
    ap = ArgumentParser()
    ap.add_argument('-a', '--start-audio')
    ap.add_argument('-d', '--drink-samples')
    ap.add_argument('-e', '--end-audio')
    ap.add_argument('-f', '--small-font-size', default=50, type=int)
    ap.add_argument('-F', '--large-font-size', default=300, type=int)
    ap.add_argument('-g', '--shot-gap', default=60, type=int)
    ap.add_argument('-s', '--shots', default=100, type=int)
    ap.add_argument('-w', '--which-path', default='/usr/bin/which')
    args = ap.parse_args(args=argv[1:])

    finder = ExecutableFinder(args.which_path)

    try:
        play_path = finder.find('play')
    except OSError as e:
        print(e, file=sys.stderr)
        play_path = None

    if play_path:
        try:
            pacmd_path = finder.find('pacmd')
        except OSError as e:
            print(e, file=sys.stderr)
            spotify = None
        else:
            spotify = Spotify(pacmd_path)
        media_controller = MediaController(Player(play_path), spotify=spotify)
        media_controller.start()
    else:
        media_controller = None

    if args.drink_samples:
        drink_samples = args.drink_samples.split(':')
    else:
        drink_samples = None

    tkinter.NoDefaultRoot()
    centurion_gui = CenturionGui(
        shot_count=args.shots,
        shot_gap_seconds=args.shot_gap,
        drink_sample_paths=drink_samples,
        player=media_controller,
        start_audio_path=args.start_audio,
        end_audio_path=args.end_audio,
        large_font_size=args.large_font_size,
        small_font_size=args.small_font_size)
    centurion_gui.mainloop()

    if media_controller:
        media_controller.join()

if __name__ == '__main__':
    exit(main())
