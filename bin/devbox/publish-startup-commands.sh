#!/bin/bash

SSH="ssh"

if [ -n "$1" ]; then
    SSH=$1
fi

scp target/* uploads@developer-app.internal.atlassian.com:~/static/