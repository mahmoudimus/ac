#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 version username"
    exit 1
fi

VERSION=$1
USER=$2

PLUGIN=atlassian-connect-plugin

echo -n "Password: "
read -s PASSWORD
echo ""

echo "Promoting atlassian-connect:$VERSION to jira dev"
curl -u$USER:$PASSWORD -XPOST https://manifesto.uc-inf.net/api/env/jirastudio-dev/product/jira/plugin/$PLUGIN -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
echo ""
echo "Promoting atlassian-connect:$VERSION to conf dev"
curl -u$USER:$PASSWORD -XPOST https://manifesto.uc-inf.net/api/env/jirastudio-dev/product/confluence/plugin/$PLUGIN -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
