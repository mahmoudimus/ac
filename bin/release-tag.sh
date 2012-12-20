#!/bin/sh

echo "Reading release.properties"

source ./release.properties

echo "Writing private key"
echo "$1" > ssh.key

export GIT_SSH=ssh-with-key.sh

#-------- branching
echo "Branching $VERSION"
git checkout -b remotable-plugins-$VERSION.x

echo "Pushing branch"
git push origin remotable-plugins-$VERSION.x

#-------- tagging
echo "Tagging $VERSION"
git tag remotable-plugins-$VERSION

echo "Pushing tag"
git push origin remotable-plugins-$VERSION
