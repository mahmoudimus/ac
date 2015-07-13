#!/bin/sh

set -e

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

if [ `contains "${PWD}" "/bin"` ]
then
    cd ..
fi

PREFIX="--> "
SNAPSHOT="-SNAPSHOT"
STARTING_VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
NEW_VERSION=`echo ${STARTING_VERSION} | sed "s/${SNAPSHOT}//"`
echo "${PREFIX} next release version: '${NEW_VERSION}'"
echo "${PREFIX} switching to master branch"
git remote set-url origin $bamboo_planRepository_repositoryUrl
git fetch origin master
git checkout master

if ! [ -z $bamboo_release_build_revision ] && [ $bamboo_release_build_revision != "true" ]
then
    COMMIT=$bamboo_planRepository_revision
else
    COMMIT="develop"
fi

echo "${PREFIX} merging ${COMMIT} into master"
git merge $COMMIT
echo "${PREFIX} pushing master"
git push origin master

echo "${PREFIX} switching back to ${COMMIT}"
git checkout $COMMIT

echo "${PREFIX} incrementing -SNAPSHOT version in poms"
mvn --batch-mode release:update-versions -DautoVersionSubmodules=true versions:update-child-modules
NEW_SNAPSHOT_VERSION=$(mvn -npu org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | grep -iv 'download' | grep -ve '[0-9]*/[0-9]*K')
POM_FILENAMES=`git status --porcelain | grep " M .*pom.xml" | sed "s/ M //"`
echo "${PREFIX} pom files: ${POM_FILENAMES}"
echo "${PREFIX} git-adding pom files"
git add ${POM_FILENAMES}
echo "${PREFIX} git-committing pom files"
git commit -m "advanced snapshot version from ${STARTING_VERSION} to ${NEW_SNAPSHOT_VERSION}" ${POM_FILENAMES}
echo "${PREFIX} git-pushing develop branch to origin"
git push origin develop
echo "${PREFIX} done!"
exit 0
