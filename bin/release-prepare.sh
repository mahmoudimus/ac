#!/bin/sh

echo "Reading release.properties from `pwd`"
source ./release.properties

echo "Updating source"
git pull origin master

echo "Checking out $GIT_SHA1.  If this fails, it is due to this build being a merge."
git checkout $GIT_SHA1
