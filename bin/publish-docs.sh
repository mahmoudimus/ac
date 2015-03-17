#!/bin/bash

DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"
$NEW_VERSION="1.1.26"

#ssh "$DESTINATIONHOST" "cd $DESTINATIONPATH; ln -sfn ./$NEW_VERSION latest; ls -l"

ln -sfn ./$NEW_VERSION latest
rsync -avz -e 'ssh' latest "$DESTINATIONHOST:$DESTINATIONPATH"

echo "'latest' symlink updated."