#!/usr/bin/env python
from os.path import abspath, join, split, splitext
from multiprocessing import Process, Pipe
import os
import sys

import pygame
from pygame.locals import *

def rpath(p):
    return abspath(join(split(__file__)[0], p))

class LiftDoor(pygame.sprite.Sprite):
    def __init__(self, image, open_left=True):
        pygame.sprite.Sprite.__init__(self)
        self.image = image
        self.rect = self.image.get_rect()
        self.offset = 0
        self.open_doors = False
        self.open_left = open_left
        self.limit = 100

    def update(self):
        if self.open_doors and self.offset < self.limit:
            offset = self.offset if not self.open_left else -1 * self.offset
            self.rect.move_ip(offset, 0)
            self.offset += 1


class LiftGame(object):
    default_mode = (1024, 768)

    def __init__(self, fullscreen=False, mode=None, tick=30):
        super(LiftGame, self).__init__()
        self.fullscreen = fullscreen
        self.mode = mode or self.default_mode
        self.tick = tick
        self.loop = True

        doors = []
        for item in os.listdir(rpath('.')):
            name, ext = splitext(item)
            if ext in ('.png',):
                img = pygame.image.load(rpath(item))
                if 'door' in name:
                    doors.append(LiftDoor(img, open_left='left' in name))
                elif 'background' in name:
                    self.background = img
        print(doors)
        self.doors = pygame.sprite.RenderPlain(tuple(doors))
        self.lift_music = pygame.mixer.Sound(rpath('lift-music.wav'))

    def handle_events(self):
        for event in pygame.event.get():
            if event.type == QUIT:
                self.loop = False
            elif event.type == KEYDOWN:
                if event.key == K_ESCAPE:
                    self.loop = False
                elif event.key == K_o:
                    for door in self.doors:
                        door.open_doors = True

    def start(self):
        self.loop = True

        self.screen = pygame.display.set_mode(self.mode)
        if self.fullscreen:
            pygame.display.toggle_fullscreen()
        pygame.mouse.set_visible(False)

        clock = pygame.time.Clock()

        #self.lift_music.play()

        while self.loop:
            self.handle_events()
            self.screen.blit(self.background, (0, 0))
            self.doors.update()
            self.doors.draw(self.screen)
            pygame.display.flip()
            clock.tick(self.tick)


def main_game():
    pygame.init()

    lift_game = LiftGame(fullscreen=True)
    lift_game.start()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    sys.path.append(split(__file__)[0])

    try:
        import button_server
        posts = Pipe()
        ts = Process(target=button_server, args=(posts,))
        ts.start()
    except Exception, e:
        print e

    main_game()

    return 0

if __name__ == '__main__':
    exit(main())

