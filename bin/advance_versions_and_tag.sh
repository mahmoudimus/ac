#!/bin/sh

# contains(string, substring)
#
# Returns 0 if the specified string contains the specified substring,
# otherwise returns 1.
contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "$string"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

contains `pwd` "/bin" ] && cd ..

PREFIX="--> "
SNAPSHOT="-SNAPSHOT" \
STARTING_VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K') \
    && NEW_VERSION=`echo $STARTING_VERSION | sed "s/${SNAPSHOT}//"` \
    && echo "${PREFIX} Setting new version in poms: ${NEW_VERSION}" \
    && mvn versions:set -DnewVersion=${NEW_VERSION} \
    && POM_FILENAMES=`git status --porcelain | grep " M .*pom.xml" | sed "s/ M //"` \
    && echo "${PREFIX} pom files: ${POM_FILENAMES}" \
    && echo "${PREFIX} git-adding pom files" \
    && git add $POM_FILENAMES \
    && echo "${PREFIX} git-committing pom files" \
    && git commit -m "removed -SNAPSHOT suffix; release version is ${NEW_VERSION}" $POM_FILENAMES \
    && echo "${PREFIX} switching to master branch" \
    && git checkout master \
    && echo "${PREFIX} merging develop into master; this will cause the Freezer plan to start the release" \
    && git merge develop \
    && echo "${PREFIX} git-tagging $NEW_VERSION" \
    && git tag $NEW_VERSION \
    && echo "${PREFIX} git-pushing master branch to origin" \
    && git push --tags origin master \
    && echo "${PREFIX} switching back to develop" \
    && git checkout develop \
    && NEW_SNAPSHOT_VERSION="${NEW_VERSION}${SNAPSHOT}" \
    && echo "${PREFIX} Setting new version in poms: $NEW_SNAPSHOT_VERSION" \
    && mvn versions:set -DnewVersion=${NEW_SNAPSHOT_VERSION} \
    && echo "${PREFIX} git-pushing develop branch to origin" \
    && git push origin develop \
    && echo "${PREFIX} done!" \
    && exit 0

# fallback on errors
echo "${PREFIX} There were errors; exiting with status 1!"
exit 1