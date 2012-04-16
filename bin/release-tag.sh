#!/bin/sh

echo "Reading release.properties"

source ./release.properties

echo "Tagging $VERSION"
git tag remoteapps-plugin-$VERSION

echo "Pushing tag"
git push origin master
