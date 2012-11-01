#!/bin/bash

usage() {
  echo "Usage: p3 start [-drp] <app key>" >&2
  if [ ! -z $1 ]; then
    echo "Unrecognized option $1." >&2
  fi
  # @todo more help info on opts, etc
  exit 1
}

if [ $# -eq 0 ]; then
  usage
fi

while getopts ":hdrp-:" optchar; do
  case "$optchar" in
    h)
      usage
      ;;
    d)
      DEBUG=1;
      shift $((OPTIND-1))
      ;;
    r)
      REBUILD=1;
      shift $((OPTIND-1))
      ;;
    p)
      PRODUCTION=1;
      shift $((OPTIND-1))
      ;;
    -)
      case "$OPTARG" in
        help)
          usage
          ;;
        debug)
          DEBUG=1;
          shift $((OPTIND-1))
          ;;
        rebuild)
          REBUILD=1;
          shift $((OPTIND-1))
          ;;
        production)
          PRODUCTION=1;
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

if [ $# -eq 0 ]; then
  usage
fi

if [ "$REBUILD" == "1" ]; then
  $P3_EXEC rebuild -c container
  if [ $? -ne 0 ]; then
    exit 1
  fi
fi

# build path to the app to be run
CWD=`pwd`
APP_PATH="$CWD/$1"
shift

APP_EXT=`echo $1 | awk -F . '{print $NF}'`
if [ "$APP_EXT" == "jar" ]; then
  JAR_ONLY=1
fi
POM_XML=$APP_PATH/pom.xml
if [ "$JAR_ONLY" == "1" ] || [ -e "$POM_XML" ]; then
  APP_KIT=servlet
else
  unset POM_XML
fi

# export oauth private key into the environment if it exists
KEY_DIR=$APP_PATH/oauth
PRIVATE_KEY=$APP_PATH/oauth/private_key
if [ -e "$PRIVATE_KEY" ]; then
  export OAUTH_LOCAL_PRIVATE_KEY=`cat $PRIVATE_KEY`
fi

# switch to working dir
WORK=$P3_HOME/work
if [ ! -d "$WORK" ]; then
  mkdir -p "$WORK"
fi
cd "$WORK"

if [ "$PRODUCTION" == "1" ]; then
  SERVER_MODE=production
else
  SERVER_MODE=development
fi

if [ "$SERVER_MODE" == "development" ]; then
  SERVER_MODE_ARGS=(-Datlassian.dev.mode=true)
  if [ "$APP_KIT" == "servlet" ] && [ "$JAR_ONLY" != "1" ]; then
    SERVER_MODE_ARGS=(${SERVER_MODE_ARGS[@]} -Dplugin.resource.directories=$APP_PATH/src/main/resources)
  fi
fi

if [ "$APP_KIT" == "servlet" ] && [ "$JAR_ONLY" != "1" ]; then
  cd $APP_PATH
  $MVN package
  if [ $? -ne 0 ]; then
    echo "Failed to package app." >&2
    exit 1
  fi
  cd -
  # FIXME this is weak (assumes desired values are first occurrences in file)
  JAR_ARTIFACT_ID=`grep -m1 "<artifactId" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
  JAR_VERSION=`grep -m1 "<version" $POM_XML | cut -f2 -d ">" | cut -f1 -d "<"`
  if [ -e "$APP_PATH/target/${JAR_ARTIFACT_ID}-${JAR_VERSION}.jar" ]; then
    APP_PATH=$APP_PATH/target/${JAR_ARTIFACT_ID}-${JAR_VERSION}.jar
  elif [ -e "$APP_PATH/target/${JAR_ARTIFACT_ID}.jar" ]; then
    APP_PATH=$APP_PATH/target/${JAR_ARTIFACT_ID}.jar
  else
    echo "No suitable application jar found." >&2
    exit 1
  fi
fi

# start
JAR_PATH="$RP_HOME/container/target/remotable-plugins-container-$PLUGIN_VERSION-standalone.jar"
if [ "$DEBUG" == "1" ]; then
  DEBUG_ARGS=(-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5004)
  RUN_MODE=debug
else
  RUN_MODE=run
fi
echo "Starting container ($RUN_MODE, $SERVER_MODE)..." >&2
java ${DEBUG_ARGS[@]} ${SERVER_MODE_ARGS[@]} -jar "$JAR_PATH" "$APP_PATH" "$@"
