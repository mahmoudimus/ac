#!/bin/bash

#This script publishes the docs to the server, in a directory corresponding to the mvn build version number.
#Run with --updateSymlink to also point the "latest" symlink to the version being published.

DIRECTORY=docs/target/gensrc/www

if [ ! -d "$DIRECTORY" ]; then
  # Control will enter here if $DIRECTORY doesn't exist.
    echo "Could not find generated documentation to deploy from $DIRECTORY"
    exit 1
fi

#GET THE MVN BUILD VERSION NUMBER
VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
NEW_VERSION=`echo ${VERSION} | sed "s/-SNAPSHOT//"`

echo "${NEW_VERSION}"

if [ -z "${NEW_VERSION}" ]; then
    # Control will enter here if $VERSION not specified.
    echo "Could not determine version from pom.xml"
    exit 2
fi

#SET THE DESTINATION PATH ON THE NEXT FILE SYSTEM
DESTINATIONHOST="uploads@developer-app.internal.atlassian.com"
DESTINATIONPATH="/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"

echo "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"

rsync -avz --delete -e 'ssh' $DIRECTORY/* "$DESTINATIONHOST:$DESTINATIONPATH/$NEW_VERSION"

if [ "$1" == "--updateSymlink" ]; then
	ssh "$DESTINATIONHOST" "cd $DESTINATIONPATH; ln -sfn ./$NEW_VERSION latest"
	echo "'latest' symlink now points to $NEW_VERSION."
else
    echo "'latest' symlink not updated."
fi

echo "Docs published to https://developer.atlassian.com/static/connect/docs/$NEW_VERSION"