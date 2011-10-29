#!/usr/bin/env python
from os.path import abspath, join, split
from multiprocessing import Process
import sys

import pygame
from pygame.locals import *

def rpath(p):
    return abspath(join(split(__file__)[0], p))

class LiftGame(object):
    default_mode = (1024, 768)

    def __init__(self, fullscreen=False, mode=None, tick=30):
        super(LiftGame, self).__init__()
        self.fullscreen = fullscreen
        self.mode = mode or self.default_mode
        self.tick = tick
        self.loop = True

        self.bg = pygame.image.load(rpath('elevator.png'))

    def handle_events(self):
        for event in pygame.event.get():
            if event.type == QUIT or (event.type == KEYUP
                    and event.key == K_ESCAPE):
                self.loop = False

    def start(self):
        self.loop = True

        self.screen = pygame.display.set_mode(self.mode)
        if self.fullscreen:
            pygame.display.toggle_fullscreen()
        pygame.mouse.set_visible(False)

        clock = pygame.time.Clock()

        while self.loop:
            self.handle_events()
            self.screen.blit(self.bg, (0, 0))
            pygame.display.flip()
            clock.tick(self.tick)


def main_game():
    pygame.init()

    lift_game = LiftGame(fullscreen=True)
    lift_game.start()


def main_ts():
    pass

def main(argv=None):
    if argv is None:
        argv = sys.argv
    ts = Process(target=main_ts)
    ts.start()

    main_game()

    return 0

if __name__ == '__main__':
    exit(main())

