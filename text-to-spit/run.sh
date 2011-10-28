#!/bin/bash

sudo python3 tts.py \
    -b 90 \
    -B backing.wav \
    -H `hostname -I` \
    -p 80 \
    -t 5 \
    -T 0.0 \
    -r "Thanks for the text, enjoy your day and for more on Man-UP visit man-up.appspot.com" \
    -m 'r_7
                                                        B3_1/2  A3_1/2
        B3_1/2  Gb3_1/2 D3_1/2  Gb3_1/2 B2              B3_1/2  A3_1/2
        B3_1/2  Gb3_1/2 D3_1/2  Gb3_1/2 B2              B3_1/2  Db4_1/2
        D4_1/2  Db4_1/2 D4_1/2  B3_1/2  Db4_1/2 B3_1/2  Db4_1/2 A3_1/2
        B3_1/2  A3_1/2  B3_1/2  G3_1/2  B2              B3_1/2  A3_1/2
        B3_1/2  Gb3_1/2 D3_1/2  Gb3_1/2 B2              B3_1/2  A3_1/2
        B3_1/2  Gb3_1/2 D3_1/2  Gb3_1/2 B2              B3_1/2  Db4_1/2
        D4_1/2  Db4_1/2 D4_1/2  B3_1/2  Db4_1/2 B3_1/2  Db4_1/2 A3_1/2
        B3_1/2  A3_1/2  B3_1/2  Db4_1/2 D4              Gb4_1/2 E4_1/2
        Gb4_1/2 D4_1/2  A3_1/2  D4_1/2  Gb3             Gb4_1/2 E4_1/2
        Gb4_1/2 D4_1/2  A3_1/2  D4_1/2  Gb3             Gb4_1/2 Ab4_1/2
        A4_1/2  Ab4_1/2 A4_1/2  Gb4_1/2 Ab4_1/2 Gb4_1/2 Ab4_1/2 E4_1/2
        Ab4_1/2 E4_1/2  Db4_1/2 E4_1/2  Ab4'


