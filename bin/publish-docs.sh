#!/bin/bash

SSH="ssh"

if [ -n "$1" ]; then
    SSH=$1
fi

#GET THE MVN BUILD VERSION NUMBER
VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
NEW_VERSION=`echo ${VERSION} | sed "s/-SNAPSHOT//"`

cd docs

if [ -z "${NEW_VERSION}" ]; then
    # Control will enter here if $VERSION not specified.
    echo "VERSION not specified!"
    exit 1
fi

npm i
npm run-script build

#SET THE DESTINATION PATH ON THE NEXT FILE SYSTEM
DESTINATION="uploads@developer-app.internal.atlassian.com:/opt/j2ee/domains/atlassian.com/developer-prod/static-content/static/connect/docs/"

rsync -avz --delete -e 'ssh' target/www/* "$DESTINATION/$NEW_VERSION"

echo "Done!"