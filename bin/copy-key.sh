#!/bin/sh

echo "Writing private key"
echo "$1" > ./.git/ssh.key.raw
cat ./.git/ssh.key.raw | sed 's/\\n/\n/g' > ./.git/ssh.key

chmod 400 ./.git/ssh.key

echo "Making ssh-with-key.sh executable"
chmod 755 ./bin/ssh-with-key.sh
export GIT_SSH=./bin/ssh-with-key.sh

exit 0
