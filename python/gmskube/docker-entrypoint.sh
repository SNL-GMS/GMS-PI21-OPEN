#!/bin/bash

# Link the oracle-wallet override directory if it exists
if [ -d "/oracle-wallet" ]; then
    ln -s /oracle-wallet /deploy/ian/oracle-wallet-override
    ln -s /oracle-wallet /deploy/sb/oracle-wallet-override
fi

# Change the gms user uid to the uid passed in from the bash wrapper.
# Default to 0 to maintain backwards compatibility with an older gmskube bash wrapper.
# Eventually this should be changed to default 1001.
usermod -u ${LOCAL_UID:-0} -o gms

# use setpriv to un-elevate to the gms user and group
HOME=/opt/gms exec setpriv --reuid=gms --regid=gms --init-groups "gmskube_cli.py" "$@"
