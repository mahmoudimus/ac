#!/bin/bash

cd docs
npm i
npm run-script build
rsync -avz --delete -e 'ssh' target/www/* uploads@developer-app.internal.atlassian.com:/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs
