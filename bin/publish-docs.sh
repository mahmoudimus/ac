#!/bin/bash

SSH="ssh"

if [ -n "$1" ]; then
    SSH=$1
fi

cd docs
npm i
npm run-script build

VERSION=

if [ -z "${VERSION}" ]; then
    echo "VERSION not specified!"
    exit 1
fi

rsync -avz --delete -e 'ssh' target/www/* uploads@developer-app.internal.atlassian.com:/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/$VERSION/docs/

# symlink latest -> latest doc version

