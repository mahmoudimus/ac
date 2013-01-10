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
  local fromFile=$2
  local toFile=$3
  if [ ! -z $4 ]; then
    fromFile=$fromFile.$4
    toFile=$toFile.$4
  fi
  local infile=$dir/$fromFile
  OUTFILE=$APP_HOME/$toFile
  cat $infile |\
    awk -v srch='@app_key@' -v repl="$APP_KEY" '{ sub(srch,repl,$0); print }' |\
    awk -v srch='@app_name@' -v repl="$APP_NAME" '{ sub(srch,repl,$0); print }' |\
    awk -v srch='@app_desc@' -v repl="$APP_DESC" '{ sub(srch,repl,$0); print }' |\
    awk -v srch='@plugin_version@' -v repl="$PLUGIN_VERSION" '{ sub(srch,repl,$0); print }' \
    > $OUTFILE
}

copyFile () {
  copyTemplate $1 $2 $2 $3
  echo "Created: $OUTFILE"
  unset OUTFILE
}

copyFileTo () {
  copyTemplate $1 $2 $3 $4
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

createDescriptor() {
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
  echo "        <oauth>" >> $outfile
  echo "            <public-key>" >> $outfile
  echo "$APP_PUBLIC_KEY" >> $outfile
  echo "            </public-key>" >> $outfile
  echo "        </oauth>" >> $outfile
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

if [ -d $APP_HOME ]; then
  echo "App $APP_KEY already exists." >&2
  exit 1
fi

TMPL_BASE_DIR=$P3_BIN/templates
TMPL_COMMON_DIR=$TMPL_BASE_DIR/common

PUBLIC_DIR=public
PUBLIC_JS_DIR=$PUBLIC_DIR/js
PUBLIC_CSS_DIR=$PUBLIC_DIR/css
LIB_DIR=lib
KEY_DIR=oauth

DESCRIPTOR=atlassian-plugin
SCRIPT_CLIENT=$PUBLIC_JS_DIR/client
STYLESHEET_STYLES=$PUBLIC_CSS_DIR/styles
LIB_README=$LIB_DIR/README

mkdir $APP_HOME

createKeys
createDescriptor

if [ "$SERVLET_KIT" == "1" ]; then
  TMPL_DIR=$TMPL_BASE_DIR/servlet-kit
  RESOURCES_DIR=src/main/resources
  VIEWS_DIR=$RESOURCES_DIR/views
  VIEW_GENERAL=$VIEWS_DIR/general-page
  JAVA_DIR=src/main/java
  SERVLETS_DIR=$JAVA_DIR/servlets
  JAVA_SERVLET=$SERVLETS_DIR/GeneralPageServlet
  mkdir -p $APP_HOME/$RESOURCES_DIR/$PUBLIC_JS_DIR
  mkdir -p $APP_HOME/$RESOURCES_DIR/$PUBLIC_CSS_DIR
  mkdir -p $APP_HOME/$LIB_DIR
  mkdir -p $APP_HOME/$VIEWS_DIR
  mkdir -p $APP_HOME/$SERVLETS_DIR
  copyFileTo $TMPL_COMMON_DIR $SCRIPT_CLIENT $RESOURCES_DIR/$SCRIPT_CLIENT js
  copyFileTo $TMPL_COMMON_DIR $STYLESHEET_STYLES $RESOURCES_DIR/$STYLESHEET_STYLES css
  copyFile $TMPL_COMMON_DIR $LIB_README md
  copyFile $TMPL_DIR pom xml
  copyFile $TMPL_DIR $JAVA_SERVLET java
  copyFile $TMPL_DIR $VIEW_GENERAL vm
else
  if [ "$COFFEE" == "1" ]; then
    SCRIPT_EXT=coffee
  else
    SCRIPT_EXT=js
  fi
  if [ "$MINIMAL" == "1" ]; then
    TMPL_DIR=$TMPL_BASE_DIR/minimal
    VIEWS_DIR=views
    VIEW_GENERAL=$VIEWS_DIR/general
    mkdir -p $APP_HOME/$VIEWS_DIR
    copyFile $TMPL_DIR main $SCRIPT_EXT
    copyFile $TMPL_DIR $VIEW_GENERAL hbs
  else
    TMPL_DIR=$TMPL_BASE_DIR/structured
    APP_DIR=app
    SCRIPT_SERVER=$APP_DIR/server
    SCRIPT_CONFIG=$APP_DIR/config
    SCRIPT_ROUTES=$APP_DIR/routes
    VIEWS_DIR=$APP_DIR/views
    VIEW_GENERAL=$VIEWS_DIR/general
    mkdir -p $APP_HOME/$APP_DIR
    mkdir -p $APP_HOME/$VIEWS_DIR
    mkdir -p $APP_HOME/$PUBLIC_JS_DIR
    mkdir -p $APP_HOME/$PUBLIC_CSS_DIR
    mkdir -p $APP_HOME/$LIB_DIR
    copyFile $TMPL_DIR main $SCRIPT_EXT
    copyFile $TMPL_DIR $SCRIPT_SERVER $SCRIPT_EXT
    copyFile $TMPL_DIR $SCRIPT_CONFIG $SCRIPT_EXT
    copyFile $TMPL_DIR $SCRIPT_ROUTES $SCRIPT_EXT
    copyFile $TMPL_DIR $VIEW_GENERAL hbs
    copyFile $TMPL_COMMON_DIR $SCRIPT_CLIENT js
    copyFile $TMPL_COMMON_DIR $STYLESHEET_STYLES css
    copyFile $TMPL_COMMON_DIR $LIB_README md
  fi
fi

echo "App '$APP_KEY' successfully created."
