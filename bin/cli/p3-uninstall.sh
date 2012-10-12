#!/bin/bash

if [ "$RP_OVERRIDE" == "1" ]; then
  echo "Uninstall is not available when using a custom RP_HOME." >&2
  exit
fi

usage() {
  echo "Usage: p3 uninstall" >&2
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

if [ -d $P3_HOME ]; then
  while true; do
    read -p "Are you sure you want to uninstall p3? " yn
    case $yn in
      [Yy]*)
        rm -rf $P3_HOME
        if [ $? -eq 0 ]; then
          echo "$P3_HOME successfully uninstalled."
        else
          echo "Uninstallation of $P3_HOME failed." >&2
        fi
        exit
        ;;
      [Nn]*)
        echo "Uninstall process aborted."
        exit
        ;;
      *) echo "Please answer yes or no." >&2;;
    esac
  done
else
  echo "Nothing to uninstall." >&2
fi
