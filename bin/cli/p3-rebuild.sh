#!/bin/bash

usage() {
  echo "Usage: p3 rebuild [-cto] [plugin|container]" >&2
  if [ ! -z $1 ]; then
    echo "Unrecognized option $1." >&2
  fi
  # @todo more help info on opts, etc
  exit 1
}

GOALS=(install)
SKIP_TESTS=-DskipTests

while getopts ":hcto-:" optchar; do
  case "$optchar" in
    h)
      usage
      ;;
    c)
      GOALS=(clean ${GOALS[@]})
      shift $((OPTIND-1))
      ;;
    t)
      unset SKIP_TESTS
      shift $((OPTIND-1))
      ;;
    o)
      ONLINE=-o
      shift $((OPTIND-1))
      ;;
    -)
      case "$OPTARG" in
        help)
          usage
          ;;
        clean)
          GOALS=(clean ${GOALS[@]})
          shift $((OPTIND-1))
          ;;
        test)
          unset SKIP_TESTS
          shift $((OPTIND-1))
          ;;
        online)
          ONLINE=-o
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

rebuildAll() {
  echo "Rebuilding all..." >&2
  cd "$RP_HOME"
  CMD=($MVN ${GOALS[@]} $SKIP_TESTS $ONLINE)
  "${CMD[@]}"
  if [ $? -ne 0 ]; then
    echo "Failed to rebuild all." >&2
    exit 1
  fi
  echo "Successfully rebuilt all." >&2
}

rebuildProject() {
  local project=$1
  local projectDir="$RP_HOME/$project"
  if [ -d "$projectDir" ]; then
    local targetDir="$projectDir/target"
    echo "Rebuilding $project..." >&2
    # @todo should we really be cleaning like this?  won't the clean goal just work?
    if [ "${GOALS[0]}" == "clean" ]; then
      rm -rf $targetDir
      GOALS=${GOALS[@]:1}
    fi
    cd "$RP_HOME"
    CMD=($MVN --projects $project --also-make ${GOALS[@]} $SKIP_TESTS $ONLINE)
    "${CMD[@]}"
    if [ $? -ne 0 ]; then
      echo "Failed to rebuild $project." >&2
      exit 1
    fi
    echo "Successfully rebuilt $project." >&2
  else
    echo "Unrecognized project $project." >&2
    usage
  fi
}

if [ $# -eq 0 ]; then
  rebuildAll
else
  rebuildProject $1
fi
