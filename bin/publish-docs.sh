#!/bin/bash

DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"

ssh "$DESTINATIONHOST"
cd "$DESTINATIONPATH"
ls -l