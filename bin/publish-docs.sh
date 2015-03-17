#!/bin/bash

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
#DESTINATION="$HOME/atlassian-connect/test/ac-docs/"


rsync -avz --delete -e 'ssh' target/gensrc/www/* "$DESTINATION/$NEW_VERSION"


if [ "$1" == "updateSymlink" ]; then
    ln -sfn ./$NEW_VERSION latest
    rsync -avz -e 'ssh' latest "$DESTINATION"
fi

echo "Done!"