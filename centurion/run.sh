#!/bin/bash

SAMPLES='drink-samples/25.wav:drink-samples/22.wav:drink-samples/24.wav:drink-samples/16.wav:drink-samples/21.wav:drink-samples/27.wav:drink-samples/20.wav:drink-samples/11.wav:drink-samples/18.wav:drink-samples/17.wav:drink-samples/12.wav:drink-samples/15.wav:drink-samples/26.wav:drink-samples/4.wav:drink-samples/23.wav:drink-samples/3.wav:drink-samples/14.wav:drink-samples/28.wav:drink-samples/7.wav:drink-samples/10.wav:drink-samples/1.wav:drink-samples/8.wav:drink-samples/5.wav:drink-samples/2.wav:drink-samples/13.wav:drink-samples/6.wav:drink-samples/19.wav'

alias centurion='src/centurion.py -d "${SAMPLES}"'
alias centurion-start-at='src/centurion.py -d "${SAMPLES}" -s'

