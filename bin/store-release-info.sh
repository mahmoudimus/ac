#!/bin/sh

BUILD_NUMBER=$2
GIT_SHA1=$1

API_VERSION=`cat pom.xml | sed -n 's/.*<version>\([0-9]\.[0-9]\)\([.0-9]*.*-SNAPSHOT\).*<.*/\1/p'`
echo "Extracted API version as $API_VERSION"

CUR_Q=`cat pom.xml | sed -n 's/.*<version>\([0-9]\.[0-9]\)\([.0-9]*\)\(.*\)-SNAPSHOT.*<.*/\3/p'`
echo "Extracted qualifier as $CUR_Q"

CURRENT_VERSION=`cat pom.xml | sed -n 's/.*<version>\([.0-9]*-SNAPSHOT\)<.*/\1/p'`
echo "Extracted current version as $CURRENT_VERSION"

VERSION=${API_VERSION}.${BUILD_NUMBER}${CUR_Q}
echo "Setting new version as $VERSION"

echo "Recording version information `pwd`/release.properties"
echo "GIT_SHA1=$GIT_SHA1
API_VERSION=$API_VERSION
CURRENT_VERSION=$CURRENT_VERSION
VERSION=${API_VERSION}.${BUILD_NUMBER}${CUR_Q}" > release.properties
