#!/bin/sh

BUILD_NUMBER=$2
GIT_SHA1=$1

echo "Extracting the api version"
API_VERSION=`cat pom.xml | sed -n 's/.*API_VERSION>\([.0-9]*\)<.*/\1/p'`
VERSION=$API_VERSION.$BUILD_NUMBER

echo "Recording the version '$VERSION' in release.properties"
echo "GIT_SHA1=$GIT_SHA1
VERSION=$API_VERSION.$BUILD_NUMBER" > release.properties

echo "Writing pom.release.xml with new build number"
cat pom.xml | sed "s/BUILD_NUMBER>\(.*\)</BUILD_NUMBER>$BUILD_NUMBER</" > pom.release.xml
