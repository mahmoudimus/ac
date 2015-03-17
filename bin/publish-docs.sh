#!/bin/bash

#SET THE DESTINATION PATH ON THE NEXT FILE SYSTEM
DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"
NEWVERSION="1.1.26"
#DESTINATIONHOST="jfurler@localhost"
#DESTINATIONPATH="$HOME/atlassian-connect/test/ac-docs/"

echo "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"


ssh "$DESTINATIONHOST" "cd $DESTINATIONPATH; ln -sfn ./$NEW_VERSION test"
echo "'latest' symlink updated."

echo "Docs published to https://developer.atlassian.com/static/connect/docs"