#!/bin/bash

USER=admin
PASS=admin

INSTANCE="http://storm.dyn.syd.atlassian.com:1990/confluence"
ADDON_URL="http://localhost:8000/atlassian-connect.json"

echo -n "Retrieving UPM token..."
TOKEN=`curl -s -u$USER:$PASS -I $INSTANCE/rest/plugins/1.0/ | grep "upm-token" | cut -d : -f 2 | cut -d ' ' -f 2`
echo -e "\t$TOKEN"
echo -n "Installing add-on..."

URL="$INSTANCE/rest/plugins/1.0/?jar=false&token=$TOKEN"

curl -XPOST -u$USER:$PASS -H "Accept: application/json" -H "Content-Type: application/vnd.atl.plugins.remote.install+json" $URL -d "{ \"pluginUri\": \"$ADDON_URL\" }"
echo -e "\tdone"
