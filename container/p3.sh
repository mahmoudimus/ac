#!/bin/bash
#
# Script to make writing js-based remote apps much easier.  To get started,
# run these commands:
#
# 1. p3.sh run-confluence
# 2. p3.sh create myapp
# 3. p3.sh start myapp
#

# ----------------- start resources

# main.js
read -d '' mainJs <<"EOF"
var oauth = require('atlassian/oauth');

exports.app = function(request) {
  var hostKey = oauth.validateRequest(request);

  var body = require('atlassian/mustache').
      render("templates/hello.mustache", {
          baseUrl : oauth.getHostBaseUrl(hostKey)
          });

  return {
    status: 200,
    headers: {
      "Content-Type": "text/html"
    },  
    body: [body]
  };  
};
EOF

# hello.mustache
read -d '' helloMustache <<"EOF"
<html>
    <head>
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
    </head>
    <body>
        <h2>Hello world</h2>
        <script src="public/init.js" type="text/javascript"></script>
    </body>
</html>
EOF

# init.js
read -d '' initJs <<"EOF"
RA.init();
EOF

# p3homeInstructions
read -d '' p3homeInstructions <<"EOF"
You need to have the P3_HOME environment variable set.  It should point to a
locally cloned remoteapps-plugin repository.  Clone via:
  
  git clone git@bitbucket.org:mrdon/remoteapps-plugin.git
  
Then, you'll need to build the project:
 
  mvn install -DskipTests

EOF

# -------------- start script

if [ $# -lt 1 ]
then
    echo "Usage : $0 create|start|run-confluence|run-jira [ARGUMENTS]"
    exit 1
fi

if [ -z "$P3_HOME" ]
then
    echo "$p3homeInstructions"
    exit 1
fi

case "$1" in

run-confluence)  echo "Starting Confluence. . ."
    shift
    atlas-run-standalone --plugins com.atlassian.labs:remoteapps-plugin:0.4.9999-SNAPSHOT --product confluence $@
    ;;
debug-confluence)  echo "Starting Confluence in debug mode. . ."
    shift
    mvn amps:debug-standalone -Dplugins=com.atlassian.labs:remoteapps-plugin:0.4.9999-SNAPSHOT -Dproduct=confluence $@
    ;;
run-jira)  echo "Starting JIRA. . ."
    shift
    atlas-run-standalone --plugins com.atlassian.labs:remoteapps-plugin:0.4.9999-SNAPSHOT --product jira $@
    ;;
create)  echo  "Creating a new app. . ."
    shift
    appKey=$1
    mkdir "$appKey"
    echo "key: $appKey" > $appKey/atlassian-remote-app.yaml
    echo "name: $appKey App" >> $appKey/atlassian-remote-app.yaml
    echo "version: 1" >> $appKey/atlassian-remote-app.yaml
    echo "description: A kickass $appKey App" >> $appKey/atlassian-remote-app.yaml
    echo "" >> $appKey/atlassian-remote-app.yaml
    echo "general-page:" >> $appKey/atlassian-remote-app.yaml
    echo "  - key: hello" >> $appKey/atlassian-remote-app.yaml
    echo "    name: Hello World" >> $appKey/atlassian-remote-app.yaml
    echo "    url: /hello" >> $appKey/atlassian-remote-app.yaml

    echo "$mainJs" > $appKey/main.js

    mkdir "$appKey/templates"
    echo "$helloMustache" > $appKey/templates/hello.mustache

    mkdir "$appKey/public"
    echo "$initJs" > $appKey/public/init.js

    echo "Your new app now exists in the $appKey directory.  You can run it via:"
    echo " "
    echo "  p3.sh start $appKey"
    echo " "
    ;;
start)  echo  "Starting the container. . ."
    shift
    appKey=$1
    java -jar "$P3_HOME/container/target/remoteapps-container-0.4.9999-SNAPSHOT.jar" $appKey
    ;;
debug)  echo  "Starting app in debug mode. . ."
    shift
    appKey=$1
    java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -jar "$P3_HOME/container/target/remoteapps-container-0.4.9999-SNAPSHOT.jar" $appKey
    ;;
*) echo "Unknown command: $1"
   ;;
esac
