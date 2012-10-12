#!/bin/bash

usage() {
  echo "Usage: p3 clean [refapp|jira|confluence]" >&2
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

if [ -z "$1" ]; then
  PRODUCT=$1
  shift
fi

# run the product with amps
CLEAN_PATH=$P3_HOME/amps-standalone/target
if [ -z "$PRODUCT" ]; then
  CLEAN_PATH=$CLEAN_PATH/$PRODUCT
fi
rm -rf $CLEAN_PATH/*
