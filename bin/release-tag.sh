#!/bin/sh

echo "Reading release.properties"

source ./release.properties

echo "Tagging $VERSION"
git tag remotable-plugins-$VERSION

echo "Pushing tag"
git push origin remotable-plugins-$VERSION
