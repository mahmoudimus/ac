#!/bin/sh

echo "Reading release.properties from `pwd`"
source ./release.properties

echo "Creating origin remote"
git remote add origin ssh://git@bitbucket.org/atlassian/remotable-plugins.git

echo "Updating source"
git fetch origin

echo "Checking out $GIT_SHA1 into branch.  If this fails, it is due to this build being a merge."

git checkout -b remotable-plugins-$VERSION.x $GIT_SHA1

echo "Replacing versions in the poms"
find . -type f -name "pom.xml" | xargs perl -pi -e "s/<version>$API_VERSION-SNAPSHOT/<version>$VERSION/g"

echo "Committing changes"
git commit -a -m "Release $VERSION"