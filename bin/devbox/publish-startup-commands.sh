#!/bin/bash

SSH="ssh"

if [ -n "$1" ]; then
    SSH=$1
fi

scp target/atlassian-connect.json uploads@developer-app.internal.atlassian.com:/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/