#!/bin/sh

AUTHOR=$1
EMAIL=$2

echo "Setting git committer env"
echo "Author: $AUTHOR"
echo "Email:  $EMAIL"

export GIT_AUTHOR_EMAIL=$EMAIL
export GIT_AUTHOR_NAME=$AUTHOR
export GIT_COMMITTER_EMAIL=$EMAIL
export GIT_COMMITTER_NAME=$AUTHOR

