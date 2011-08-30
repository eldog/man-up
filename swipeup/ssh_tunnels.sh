#!/bin/bash
set -o errexit

SSH_PID='/tmp/ssh_pid'

while (( "$#" )); do
    case "$1" in
        -k) KILL='kill' ;;

        -m) shift
            CS_MACHINE="$1" ;;

        -u) shift
            CS_USERNAME="$1" ;;
    esac
    shift
done

if [[ -n "${CS_MACHINE}" && -n "${CS_USERNAME}" && -z "${KILL}" ]]; then
    echo "Starting SSH tunnels to ${CS_USERNAME}"
    ssh -N -L 8000:edir.man.ac.uk:389 \
        "${CS_USERNAME}@${CS_MACHINE}.cs.man.ac.uk" &
    echo $! > "${SSH_PID}"
elif [[ -z "${CS_MACHINE}" && -z "${CS_USERNAME}" && -n "${KILL}" ]]; then
    echo 'Stopping SSH tunnels.'
    kill `cat "${SSH_PID}"`
    rm -f "${SSH_PID}"
else
    echo 'Usage: ssh_tunnels.ssh (-m <cs_machine> -u <cs_username> | -k)'
fi

