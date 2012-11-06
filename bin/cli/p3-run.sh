#!/bin/bash

usage() {
  echo "Usage: p3 run [-d] [refapp|jira|confluence]" >&2
  if [ ! -z $1 ]; then
    echo "Unrecognized option $1." >&2
  fi
  # @todo more help info on opts, etc
  exit 1
}

while getopts ":hd-:" optchar; do
  case "$optchar" in
    h)
      usage
      ;;
    d)
      DEBUG=1
      shift $((OPTIND-1))
      ;;
    -)
      case "$OPTARG" in
        help)
          usage
          ;;
        debug)
          DEBUG=1
          shift $((OPTIND-1))
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

[[ $DEBUG == 1 ]] && ACTION=debug || ACTION=run

PRODUCT=${1:-"refapp"}
shift

case "$PRODUCT" in
  refapp)
    VERSION=$REFAPP_VERSION
    ;;
  jira)
    VERSION=$JIRA_VERSION
    ;;
  confluence)
    VERSION=$CONFLUENCE_VERSION
    ;;
esac

CMD=($MVN amps:$ACTION -Dproduct=$PRODUCT)

# run the desired product
if [ "$RP_OVERRIDE" == "1" ]; then
  RUN_DIR=$RP_HOME/plugin
  CMD=(${CMD[@]} -DskipTests $@)
else
  RUN_DIR=$P3_HOME
  CMD[1]=${CMD[1]}-standalone
  PLUGIN=com.atlassian.plugins:remotable-plugins-plugin:$PLUGIN_VERSION
  CMD=(${CMD[@]} -Dproduct.version=$VERSION -Dplugins=$PLUGIN $@)
fi

cd $RUN_DIR

# need to clean plugin settings for refapp to avoid oauth app link errors
if [ "$PRODUCT" == "refapp" ]; then
  rm -rf target/refapp/home/data/com.atlassian.refapp.sal.pluginsettings.xml
fi

echo "Starting $PRODUCT..." >&2
"${CMD[@]}"
