#!/usr/bin/env python3.2
import curses
from itertools import cycle
import os
import random
import subprocess
import tempfile
import time
import types

class MiniGame:
    DEF_FPS = 25

    def __init__(self, scr):
        self.scr = scr
        self.my, self.mx = self.scr.getmaxyx()

        self.running = True
        self.fps = self.DEF_FPS

        self.listeners = {}
        for obj in (getattr(self, obj_name) for obj_name in dir(self)):
            if isinstance(obj, types.MethodType) \
            and 'return' in obj.__annotations__:
                self.listeners[obj.__annotations__['return']] = obj

        self.on_set_scr()

    def fire_event(self, key):
        if key in self.listeners:
            self.listeners[key]()

    def mainloop(self):
        while self.running:
            while True:
                c = self.scr.getch()
                if c == -1:
                    break
                self.fire_event(c)
            self.scr.clear()
            self.draw()
            self.scr.refresh()
            time.sleep(1 / self.fps)

    def on_finish(self):
        pass

    def on_set_scr(self):
        pass

    def print_centred_text(self, text):
        lines = text.split('\n')

        height = len(lines)
        width = 0
        for line in lines:
            line_length = len(line)
            if line_length > width:
                width = line_length

        y_offset = (self.my - len(lines)) // 2
        x_offset = (self.mx - width) // 2

        for y in range(height):
            for x in range(len(lines[y])):
                self.scr.addch(y + y_offset, x + x_offset, lines[y][x])


class Cowsay(MiniGame):
    COW_DIR = '/usr/share/cowsay/cows'

    COW_FILES = []
    for item in os.listdir(COW_DIR):
        item = os.path.join(COW_DIR, item)
        if os.path.isfile(item) and os.path.splitext(item)[1] == '.cow':
            COW_FILES.append(item)

    def draw(self):
        cowsay = subprocess.Popen(('/usr/games/cowsay', '-f',
                                   random.choice(self.COW_FILES),
                                   "Game Over. You're dead!"),
                                  stdin=subprocess.PIPE,
                                  stdout=subprocess.PIPE)
        cowsay.wait()
        self.print_centred_text(cowsay.stdout.read().decode('utf-8'))

    def mainloop(self):
        self.scr.clear()
        self.draw()
        self.scr.refresh()
        time.sleep(2)

class WebcamFace(MiniGame):
    IMAGE_PATH = 'image.jpeg'
    MARGIN = 5

    def draw(self):
        with open(os.devnull, 'w') as output_file:
            subprocess.check_call(('/usr/bin/streamer', '-o', self.IMAGE_PATH),
                                  stderr=output_file)
            jp2a = subprocess.Popen(('/usr/bin/jp2a', self.dimension_arg,
                                     self.IMAGE_PATH),
                                    stderr=output_file, stdout=subprocess.PIPE)
            if jp2a.returncode:
                raise subprocess.CalledProcessError()
            jp2a.wait()
        self.print_centred_text(jp2a.stdout.read().decode('utf-8'))

    def mainloop(self):
        # Emtpy buffer
        while self.scr.getch() != -1:
            pass
        while self.running:
            if self.scr.getch() != -1:
                break
            self.scr.clear()
            self.draw()
            self.scr.refresh()
            time.sleep(1 / self.fps)

    def on_set_scr(self):
        self.image = tempfile.NamedTemporaryFile(mode='w')
        self.dimension_arg = '--%s=%d' % ('height'
                                          if self.my < self.mx else 'width',
                                          min((self.my, self.mx)) - self.MARGIN)


class Car(MiniGame):
    DISTANCE_PER_LEVEL = 50
    INITIAL_ROAD_WIDTH_PERCENTAGE = 25
    PAD_TOP = 1
    PAD_BOTTOM = 1
    PAD_TOTAL = PAD_TOP + PAD_BOTTOM

    CAR = '&'
    CAR_DISTANCE_FROM_BOTTOM = 10

    def pre_shrink_next(self):
        return self.dist_travelled_this_level == self.DISTANCE_PER_LEVEL - 2

    def left_limit(self, ps):
        if self.pre_shrink_next():
            ns = self.pre_shrink
        else:
            ns = random.choice((self.left_limit, self.right))
        return (ns, ps[1], ps[2], '|', '|')

    def left(self, ps):
        if self.pre_shrink_next():
            ns = self.pre_shrink
        elif ps[1] - 1 == 0:
            ns = self.left_limit
        else:
            ns = random.choice((self.left, self.straight))
        return (ns, ps[1] - 1, ps[2], '\\', '\\')

    def straight(self, ps):
        if self.pre_shrink_next():
            ns = self.pre_shrink
        else:
            ns = random.choice((self.left, self.straight, self.right))
        return (ns, ps[1], ps[2], '|', '|')

    def right(self, ps):
        if self.pre_shrink_next():
            ns = self.pre_shrink
        if ps[1] + ps[2] + 1 == self.mx - 1:
            ns = self.right_limit
        else:
            ns = random.choice((self.straight, self.right))
        return (ns, ps[1] + 1, ps[2], '/', '/')

    def right_limit(self, ps):
        if self.pre_shrink_next():
            ns = self.pre_shrink(ps)
        else:
            ns = random.choice((self.left, self.right_limit))
        return (ns, ps[1], ps[2], '|', '|')

    def pre_shrink(self, ps):
        return (self.shrink, ps[1], ps[2], '|', '|')

    def shrink(self, ps):
        return (self.straight, ps[1] + 1, ps[2] - 2, '/', '\\')

    def on_set_scr(self):
        self.distance_travelled = 0
        self.width = self.mx * self.INITIAL_ROAD_WIDTH_PERCENTAGE // 100

        curb_left_x = (self.mx - self.width) // 2

        self.road_length = self.my - self.PAD_TOTAL
        self.road = [(self.straight, curb_left_x, self.width, '|', '|')
                     for self.road_end in range(self.road_length)]
        self.road_start = (self.road_end + 1) % self.road_length

        self.car_y = self.my - self.CAR_DISTANCE_FROM_BOTTOM
        self.car_x = curb_left_x + self.width // 2

    def draw(self):
        self.distance_travelled += 1
        self.dist_travelled_this_level = \
            self.distance_travelled % self.DISTANCE_PER_LEVEL

        ps = self.road[self.road_start]
        self.road[self.road_end] = ps[0](ps)
        self.road_start = self.road_end
        self.road_end = (self.road_end - 1) % self.road_length

        for i in range(len(self.road)):
            _ns, x, w, ls, rs = self.road[
                (i + self.road_start) % len(self.road)]
            self.scr.addch(i + self.PAD_TOP, x, ls)
            self.scr.addch(i + self.PAD_TOP, x + w, rs)

        cs = self.road[(self.car_y + self.road_start) % len(self.road)]
        if cs[1] >= self.car_x:
            self.car_x = cs[1]
            self.running = False
        elif cs[1] + cs[2] <= self.car_x:
            self.car_x = cs[1] + cs[2]
            self.running = False

        self.scr.addch(self.car_y, self.car_x, self.CAR)

    def mainloop(self):
        while self.running:
            while True:
                c = self.scr.getch()
                if c == -1:
                    break
                self.fire_event(c)
            self.scr.clear()
            self.draw()
            self.scr.refresh()
            time.sleep(1 / self.fps)

    def l_key_left(self) -> curses.KEY_LEFT:
        if self.car_x != 0:
            self.car_x -= 1

    def l_key_right(self) -> curses.KEY_RIGHT:
        if self.car_x != self.mx - 1:
            self.car_x += 1


def main(scr):
    curses.curs_set(False)
    scr.nodelay(True)
    for game in cycle((WebcamFace, Car, Cowsay)):
        g = game(scr)
        g.mainloop()
        g.on_finish()


if __name__ == '__main__':
    try:
        curses.wrapper(main)
    except KeyboardInterrupt:
        pass

