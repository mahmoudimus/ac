#!/bin/bash

if [ -z "$1" ]; then
    echo "Please provide a version and username to deploy as"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Please provide a user to deploy as"
    exit 1
fi


VERSION=$1
USER=$2

echo "Promoting atlassian-connect:$VERSION to jira dev"
curl -u$USER -XPOST https://manifesto.uc-inf.net/api/env/jirastudio-dev/product/jira/plugin/atlassian-connect-plugin -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
echo "Promoting atlassian-connect:$VERSION to conf dev"
curl -u$USER -XPOST https://manifesto.uc-inf.net/api/env/jirastudio-dev/product/confluence/plugin/atlassian-connect-plugin -H"Content-Type: application/json" -d "{ \"version\": \"$VERSION\" }"
