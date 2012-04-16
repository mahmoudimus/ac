#!/bin/sh

echo "Reading release.properties from `pwd`"
source ./release.properties

echo "Checking out $GIT_SHA1..."
git checkout $GIT_SHA1
