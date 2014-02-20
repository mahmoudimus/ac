#!/bin/bash

SSH='ssh'

if [ -z "$1" ]; then
    SSH=$1
fi

cd docs
npm i
npm run-script build
rsync -avz --delete -e $SSH target/www/* uploads@developer-app.internal.atlassian.com:/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs
