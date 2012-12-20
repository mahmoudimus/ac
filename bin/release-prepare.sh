#!/bin/sh

echo "Reading release.properties from `pwd`"
source ./release.properties

echo "Updating source"
git fetch origin

echo "Checking out $GIT_SHA1.  If this fails, it is due to this build being a merge."
git checkout $GIT_SHA1

echo "Branching $VERSION"
git checkout -b remotable-plugins-$VERSION.x

echo "Replacing versions in the poms"
find . -type f -name "pom.xml" | xargs perl -pi -e "s/<version>$API_VERSION-SNAPSHOT/<version>$VERSION/g"

echo "Committing changes"
git commit -a -m "Release $VERSION"