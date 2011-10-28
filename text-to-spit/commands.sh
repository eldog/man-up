#!/bin/bash

HOSTNAME="`hostname -I`"

echo "Hostname: ${HOSTNAME}"

function docurl
{
    local number="${1}"; shift;
    curl -s \
        -d number="${number}" \
        -d message="${*}" \
        -o /dev/null \
        "${HOSTNAME}:80"

}

alias user='docurl +447555555555'
alias admin='docurl admin'
alias admin-speech='admin speech'
alias admin-compile='admin compile'
alias admin-rap='admin rap'
alias run="python3 tts.py --hostname ${HOSTNAME}"

