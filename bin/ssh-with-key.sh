#!/bin/sh

CURDIR=`pwd`
LISTING=`ls -al`
KEY=`ls -al ./.git/ssh.key`
echo "$CURDIR\n" > ./.git/ssh.txt
echo "$LISTING\n" >> ./.git/ssh.txt
echo "$KEY\n" >> ./.git/ssh.txt
echo "ssh -i target/ssh.key $@ \n" >> ./.git/ssh.txt
exec ssh -i ./.git/ssh.key "$@"

exit 0
