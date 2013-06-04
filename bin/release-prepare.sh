#!/bin/sh

echo "Reading release.properties from `pwd`/release.properties"
source ./release.properties

echo "Creating origin remote"
git remote add origin ssh://git@bitbucket.org/atlassian/remotable-plugins.git

echo "Updating source"
git fetch origin

echo "Checking out $GIT_SHA1 into branch.  If this fails, it is due to this build being a merge."
git checkout -b remotable-plugins-$VERSION.x $GIT_SHA1

echo "Replacing versions in the poms, version $CURRENT_VERSION will be replaced with version $VERSION"
find . -type f -name "pom.xml" | xargs perl -pi -e "s/<version>$CURRENT_VERSION/<version>$VERSION/g"

echo "Committing changes, releasing version $VERSION"
git commit -a -m "Release $VERSION"