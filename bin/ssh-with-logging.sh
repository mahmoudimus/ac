#!/bin/sh
echo "SSH With Logging" > ./.git/ssh.txt
CURDIR=`pwd`
LISTING=`ls -al`
echo "$CURDIR\n" >> ./.git/ssh.txt
echo "$LISTING\n" >> ./.git/ssh.txt
echo "ssh -vvv $@ \n" >> ./.git/ssh.txt
exec ssh -vvv "$@" >> ./.git/ssh.txt

exit 0
