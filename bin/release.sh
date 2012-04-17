#!/bin/sh
die () {
   echo >&2 "$@"
   exit 1
}


[ "$#" -eq 2 ] || die "Usage - release.sh GIT_SHA1 BUILD_NUMBER" 

bin/store-release-info.sh $@
bin/release-prepare.sh 
mvn -f pom.release.xml -Prelease clean deploy -DskipTests
bin/release-tag.sh

