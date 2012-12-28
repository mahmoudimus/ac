#!/bin/sh

BUILD_NUMBER=$2
GIT_SHA1=$1

echo "Extracting the api version"
API_VERSION=`cat pom.xml | sed -n 's/.*<api.version>\([.0-9]*\)<.*/\1/p'`
VERSION=$API_VERSION.$BUILD_NUMBER

echo "Recording the version '$VERSION' in release.properties"
echo "GIT_SHA1=$GIT_SHA1
API_VERSION=$API_VERSION
VERSION=$API_VERSION.$BUILD_NUMBER" > release.properties
