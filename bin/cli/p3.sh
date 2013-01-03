#!/bin/bash

export MVN=atlas-mvn

export P3_HOME=~/.p3

export RP_REPO=https://bitbucket.org/atlassian/remotable-plugins.git
if [ -z "$RP_HOME" ]; then
  export RP_HOME="$P3_HOME/remotable-plugins"
else
  export RP_OVERRIDE=1
fi

export P3_BIN=$RP_HOME/bin/cli
export P3_EXEC=$P3_BIN/p3.sh

usage() {
  echo "Usage: p3 [<command> [command opts]]" >&2
  # @todo more help on commands
  exit 1
}

exportVersions() {
  POM_XML=$RP_HOME/pom.xml
  export PLUGIN_API_VERSION=`grep "<api.version" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
  export PLUGIN_VERSION=${PLUGIN_API_VERSION}-SNAPSHOT
  export REFAPP_VERSION=`grep "<refapp.version" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
  export JIRA_VERSION=`grep "<jira.version" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
  export CONFLUENCE_VERSION=`grep "<confluence.version" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
}

resolveCommand() {
  # add command alias lookups here
  if [ "$1" == "c" ]; then
    CMD_NAME=create
  elif [ "$1" == "r" ]; then
    CMD_NAME=run
  elif [ "$1" == "rb" ]; then
    CMD_NAME=rebuild
  elif [ "$1" == "s" ]; then
    CMD_NAME=start
  else
    CMD_NAME=$1
  fi
}

if [ "$0" == "sh" ]; then
  # installation mode
  if [ ! -d $P3_HOME ]; then
    mkdir -p $P3_HOME
    if [ "$RP_OVERRIDE" != "1" ]; then
      # if the $P3_HOME doesn't exist, clone and run set up
      git clone $RP_REPO $P3_HOME/remotable-plugins
      if [ $? -ne 0 ]; then
        echo "Failed to clone $RP_REPO." >&2
        exit 1
      fi
    fi
    $P3_BIN/setup.sh
    if [ $? -ne 0 ]; then
      exit 1
    fi
  elif [ $# -eq 0 ]; then
    echo "Atlassian p3 is already installed." >&2
    echo "Maybe you wanted 'p3 update' or 'p3 uninstall'?" >&2
    exit 1
  else
    echo "Cannot execute p3 commands in installation mode." >&2
    exit 1
  fi
else
  # cli mode
  if [ ! -d "$P3_HOME" ]; then
    mkdir -p $P3_HOME
  fi
  if [ $# -eq 0 ]; then
    # if there were no script args, print usage and exit
    usage
  else
    # find a usable mvn command, or exit
    command -v $MVN >/dev/null 2>&1
    if [ $? -ne 0 ]; then
      SDK_HOME=`ls -d "$P3_HOME"/*-sdk-*`
      MVN="$SDK_HOME/bin/$MVN"
      command -v $MVN >/dev/null 2>&1
      if [ $? -ne 0 ]; then
        echo "WARNING: $MVN not found.  You may need to reinstall p3." >&2
      fi
    fi
    export MVN
    # export various versions into the environment
    exportVersions
    # run p3.sh command
    resolveCommand $1
    P3_CMD="$P3_BIN/p3-$CMD_NAME.sh"
    if [ -f $P3_CMD ]; then
      shift
      $P3_CMD "$@"
    else
      usage
    fi
  fi
fi
