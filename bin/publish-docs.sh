#!/bin/bash

#GET THE MVN BUILD VERSION NUMBER
VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
NEW_VERSION=`echo ${VERSION} | sed "s/-SNAPSHOT//"`

cd docs

echo "${NEW_VERSION}"

if [ -z "${NEW_VERSION}" ]; then
    # Control will enter here if $VERSION not specified.
    echo "Could not determine version from pom.xml"
    exit 1
fi

npm i
npm run-script build

#SET THE DESTINATION PATH ON THE NEXT FILE SYSTEM
DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"

echo "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"

rsync -avz --delete -e 'ssh' target/gensrc/www/* "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"


if [ "$1" == "updateSymlink" ]; then
	ssh "$DESTINATIONHOST" "cd $DESTINATIONPATH; ln -sfn ./$NEW_VERSION latest"
	echo "'latest' symlink now points to $NEW_VERSION."
fi

echo "Docs published to https://developer.atlassian.com/static/connect/docs/$NEW_VERSION"