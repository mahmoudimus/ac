#!/bin/bash

#GET THE MVN BUILD VERSION NUMBER
VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
NEW_VERSION=`echo ${VERSION} | sed "s/-SNAPSHOT//"`

cd docs

echo "${NEW_VERSION}"

if [ -z "${NEW_VERSION}" ]; then
    # Control will enter here if $VERSION not specified.
    echo "VERSION not specified!"
    exit 1
fi

npm i
npm run-script build

#SET THE DESTINATION PATH ON THE NEXT FILE SYSTEM
DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"
#DESTINATIONHOST="jfurler@localhost"
#DESTINATIONPATH="$HOME/atlassian-connect/test/ac-docs/"

echo "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"

rsync -avz --delete -e 'ssh' target/gensrc/www/* "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"


if [ "$1" == "updateSymlink" ]; then
	ssh "$DESTINATIONHOST" "cd $DESTINATIONPATH; ln -sfn ./$NEW_VERSION latest"
	echo "'latest' symlink updated."
fi

echo "Done!"