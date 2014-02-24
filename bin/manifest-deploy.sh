#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 version username"
    exit 1
fi

VERSION=$1
USER=$2

PLUGIN=atlassian-connect-plugin
ZONE=jirastudio-dev

echo -n "Password: "
read -s PASSWORD
echo ""

echo "Promoting $PLUGIN:$VERSION to jira: $ZONE"
curl -u$USER:$PASSWORD -XPOST https://manifesto.uc-inf.net/api/env/$ZONE/product/jira/plugin/$PLUGIN -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
echo ""
echo "Promoting $PLUGIN:$VERSION to conf: $ZONE"
curl -u$USER:$PASSWORD -XPOST https://manifesto.uc-inf.net/api/env/$ZONE/product/confluence/plugin/$PLUGIN -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
