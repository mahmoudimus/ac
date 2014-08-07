#!/bin/sh

HOSTNAME=$1
PASSWORD=$2
USERNAME=sysadmin

# TOKEN=`curl -s -u$USERNAME:$PASSWORD -I $HOSTNAME/rest/plugins/1.0/ | grep "upm-token" | cut -d : -f 2 | cut -d ' ' -f 2`

curl -v -XPOST -u$USERNAME:$PASSWORD -H "Accept: application/json" -H "Content-Type: application/json" "$HOSTNAME/rest/plugins/1.0/scheduler/updates"
