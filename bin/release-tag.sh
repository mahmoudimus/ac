#!/bin/sh

echo "Reading release.properties"

source ./release.properties

echo "Writing private key"
echo "$1" > ssh.key.raw
cat ssh.key.raw | sed 's/\\n/\n/g' > ssh.key

chmod 400 ssh.key
echo "Printing ssh key"
cat ssh.key

echo "Making ssh-with-key.sh executable"
chmod 755 ./bin/ssh-with-key.sh
export GIT_SSH=./bin/ssh-with-key.sh

echo "Pushing branch"
git push origin remotable-plugins-$VERSION.x

#-------- tagging
echo "Tagging $VERSION"
git tag remotable-plugins-$VERSION

echo "Pushing tag"
git push origin remotable-plugins-$VERSION
