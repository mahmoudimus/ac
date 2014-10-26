#!/bin/bash

SSH="ssh"

if [ -n "$1" ]; then
    SSH=$1
fi

if [ -d "target" ]; then
	echo "New command was created, deploying to DAC"
	scp target/* uploads@developer-app.internal.atlassian.com:~/static/
fi