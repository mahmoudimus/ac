#!/bin/sh

echo "Reading release.properties from `pwd`"
source ./release.properties

echo "Adding origin if not already present"
git remote add origin git@bitbucket.org:mrdon/remoteapps-plugin.git

echo "Updating source"
git pull origin master

echo "Checking out $GIT_SHA1..."
git checkout $GIT_SHA1
