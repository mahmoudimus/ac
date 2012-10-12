#!/bin/bash

if [ "$RP_OVERRIDE" == "1" ]; then
  echo "Update is not available when using a custom RP_HOME." >&2
  exit
fi

usage() {
  echo "Usage: p3 update" >&2
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

# @todo update implementation
# - backup old dir
# - run installation
# - if successful then delete backup dir
# - else restore backup dir and report failure

# @debug simple brain-dead impl until a real one is written
cd $RP_HOME
git fetch origin master && git reset --hard FETCH_HEAD && git clean -dfx
if [ $? == 0 ]; then
  echo "Update completed successfully." >&2
fi
