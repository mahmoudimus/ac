#!/bin/bash

usage() {
  echo "Usage: p3 create [-mocs] <app key> [<app name> [<app description>]]" >&2
  if [ ! -z $1 ]; then
    echo "Unrecognized option $1." >&2
  fi
  # @todo more help info on opts, etc
  exit 1
}

if [ $# -eq 0 ]; then
  usage
fi

copyTemplate() {
  local dir=$1
  local file=$2
  if [ ! -z $3 ]; then
    file=$file.$3
  fi
  local infile=$dir/$file
  OUTFILE=$APP_HOME/$file
  cat $infile |\
    awk -v srch='@app_key@' -v repl="$APP_KEY" '{ sub(srch,repl,$0); print }' |\
    awk -v srch='@app_name@' -v repl="$APP_NAME" '{ sub(srch,repl,$0); print }' |\
    awk -v srch='@app_desc@' -v repl="$APP_DESC" '{ sub(srch,repl,$0); print }' \
    > $OUTFILE
}

copyFile () {
  copyTemplate $1 $2 $3
  echo "Created: $OUTFILE"
  unset OUTFILE
}

createKeys() {
  if [ "$OAUTH" == "1" ]; then
    UMASK_ORIG=`umask`
    umask 077
    mkdir $APP_HOME/$KEY_DIR
    umask $UMASK_ORIG

    local private_key_file=$APP_HOME/$KEY_DIR/private_key
    local public_key_file=$APP_HOME/$KEY_DIR/public_key
    local tmp_public_key_file=$private_key_file.pub
    ssh-keygen -q -t rsa -b 2048 -N "" -f $private_key_file
    ssh-keygen -e -m PKCS8 -f $tmp_public_key_file > $public_key_file
    rm -f $tmp_public_key_file
    echo "Created: $private_key_file"
    echo "Created: $public_key_file"

    APP_PUBLIC_KEY=`awk '{ print "                " $0 }' $public_key_file`
  fi
}

writeDescriptor() {
  local outfile=$APP_HOME/$DESCRIPTOR.xml
  echo "<?xml version=\"1.0\" ?>" >> $outfile
  echo "<atlassian-plugin key=\"$APP_KEY\" name=\"$APP_NAME\" plugins-version=\"2\">" >> $outfile
  echo >> $outfile
  echo "    <plugin-info>" >> $outfile
  echo "        <description>$APP_DESC</description>" >> $outfile
  echo "        <version>1</version>" >> $outfile
  if [ "$OAUTH" == "1" ]; then
  echo "        <permissions>" >> $outfile
  echo "            <permission>create_oauth_link</permission>" >> $outfile
  echo "        </permissions>" >> $outfile
  fi
  echo "    </plugin-info>" >> $outfile
  echo >> $outfile
  echo "    <remote-plugin-container key=\"container\" display-url=\"http://localhost:8000/$APP_KEY\">" >> $outfile
  if [ "$OAUTH" == "1" ]; then
  echo "      <oauth>" >> $outfile
  echo "          <public-key>" >> $outfile
  echo "$APP_PUBLIC_KEY" >> $outfile
  echo "          </public-key>" >> $outfile
  echo "      </oauth>" >> $outfile
  fi
  echo "    </remote-plugin-container>" >> $outfile
  echo >> $outfile
  echo "    <general-page key=\"general\" name=\"$APP_NAME (general page)\" url=\"/general\" />" >> $outfile
  echo >> $outfile
  echo "</atlassian-plugin>" >> $outfile
  echo "Created: $outfile"
}

while getopts ":mocs-:" optchar; do
  case "$optchar" in
    h)
      usage
      ;;
    c)
      COFFEE=1;
      shift $((OPTIND-1))
      ;;
    o)
      OAUTH=1;
      shift $((OPTIND-1))
      ;;
    m)
      MINIMAL=1;
      shift $((OPTIND-1))
      ;;
    s)
      SERVLET_KIT=1;
      shift $((OPTIND-1))
      ;;
    -)
      case "$OPTARG" in
        help)
          usage
          ;;
        coffee)
          COFFEE=1;
          shift $((OPTIND-1))
          ;;
        oauth)
          OAUTH=1;
          shift $((OPTIND-1))
          ;;
        minimal)
          MINIMAL=1;
          shift $((OPTIND-1))
          ;;
        servlet)
          SERVLET_KIT=1;
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

APP_KEY=$1
APP_NAME=${2:-"$APP_KEY app"}
APP_DESC=${3:-"$APP_NAME description"}
APP_PERMISSIONS=""
APP_OAUTH_SECTION=""

APP_HOME=$APP_KEY
TMPL_BASE_DIR=$P3_BIN/templates
TMPL_COMMON_DIR=$TMPL_BASE_DIR/common

if [ "$MINIMAL" == "1" ]; then
  TMPL_DIR=$TMPL_BASE_DIR/minimal
else
  TMPL_DIR=$TMPL_BASE_DIR/structured
fi

PUBLIC_DIR=public
PUBLIC_JS_DIR=$PUBLIC_DIR/js
PUBLIC_CSS_DIR=$PUBLIC_DIR/css
LIB_DIR=lib
KEY_DIR=oauth

DESCRIPTOR=atlassian-plugin
SCRIPT_MAIN=main
SCRIPT_CLIENT=$PUBLIC_JS_DIR/client
STYLESHEET_CLIENT_MAIN=$PUBLIC_CSS_DIR/styles
LIB_README=$LIB_DIR/README

if [ -d $APP_HOME ]; then
  echo "App $APP_KEY already exists." >&2
  exit 1
fi

mkdir $APP_HOME

if [ "$COFFEE" == "1" ]; then
  SCRIPT_EXT=coffee
else
  SCRIPT_EXT=js
fi

createKeys
writeDescriptor
copyFile $TMPL_DIR $SCRIPT_MAIN $SCRIPT_EXT

if [ "$MINIMAL" == "1" ]; then
  VIEWS_DIR=views
  VIEW_GENERAL=$VIEWS_DIR/general
  mkdir $APP_HOME/$VIEWS_DIR
  copyFile $TMPL_DIR $VIEW_GENERAL mustache
else
  APP_DIR=app
  SCRIPT_SERVER=$APP_DIR/server
  SCRIPT_ROUTES=$APP_DIR/routes
  VIEWS_DIR=$APP_DIR/views
  VIEW_GENERAL=$VIEWS_DIR/general
  mkdir $APP_HOME/$APP_DIR
  mkdir $APP_HOME/$VIEWS_DIR
  mkdir $APP_HOME/$PUBLIC_DIR
  mkdir $APP_HOME/$PUBLIC_JS_DIR
  mkdir $APP_HOME/$PUBLIC_CSS_DIR
  mkdir $APP_HOME/$LIB_DIR
  copyFile $TMPL_DIR $SCRIPT_SERVER $SCRIPT_EXT
  copyFile $TMPL_DIR $SCRIPT_ROUTES $SCRIPT_EXT
  copyFile $TMPL_DIR $VIEW_GENERAL mustache
  copyFile $TMPL_COMMON_DIR $SCRIPT_CLIENT js
  copyFile $TMPL_COMMON_DIR $STYLESHEET_CLIENT_MAIN css
  copyFile $TMPL_COMMON_DIR $LIB_README md
fi

echo "App '$APP_KEY' successfully created."
