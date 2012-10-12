#!/bin/bash

usage() {
  echo "Usage: p3 env" >&2
  if [ ! -z $1 ]; then
    echo "Unrecognized option $1." >&2
  fi
  # @todo more help info on opts, etc
  exit 1
}

while getopts ":h-:" optchar; do
  case "$optchar" in
    h)
      usage
      ;;
    -)
      case "$OPTARG" in
        help)
          usage
          ;;
        *)
          usage "--${OPTARG}"
          ;;
      esac
      ;;
    *)
      usage "-${OPTARG}"
      ;;
  esac
done

shift

SDK_HOME=`which $MVN`
SDK_HOME=${SDK_HOME%/*/*}
SDK_VERSION=`$SDK_HOME/bin/atlas-version | grep "ATLAS Version" | cut -f2 -d ":" | tr -d ' '`

# dump env vars
echo "Current p3 runtime settings:" >&2
echo >&2
echo "  P3_HOME=$P3_HOME" >&2
echo "  P3_BIN=$P3_BIN" >&2
echo "  P3_EXEC=$P3_EXEC" >&2
echo "  RP_HOME=$RP_HOME" >&2
echo "  RP_REPO=$RP_REPO" >&2
echo "  SDK_VERSION=$SDK_VERSION" >&2
echo "  SDK_HOME=$SDK_HOME" >&2
echo "  MVN=$MVN" >&2
echo "  PLUGIN_API_VERSION=$PLUGIN_API_VERSION" >&2
echo "  PLUGIN_BUILD_NUMBER=$PLUGIN_BUILD_NUMBER" >&2
echo "  PLUGIN_VERSION=$PLUGIN_VERSION" >&2
echo "  REFAPP_VERSION=$REFAPP_VERSION" >&2
echo "  JIRA_VERSION=$JIRA_VERSION" >&2
echo "  CONFLUENCE_VERSION=$CONFLUENCE_VERSION" >&2
echo
