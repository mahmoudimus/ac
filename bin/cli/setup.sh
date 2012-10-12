#!/bin/bash

SDK_VERSION=3.11
SDK_URL=https://maven.atlassian.com/public/com/atlassian/amps/atlassian-plugin-sdk/$SDK_VERSION/atlassian-plugin-sdk-$SDK_VERSION.tar.gz

suggest() {
  echo "You may want to uninstall with '$P3_EXEC uninstall' and then try the installation again." >&2
}

command -v $MVN >/dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Installing the Atlassian Plugin SDK (v$SDK_VERSION)..." >&2
  cd "$P3_HOME"
  curl $SDK_URL | tar -zx
  if [ $? -eq 0 ]; then
    SDK_DIR=`ls -d *-sdk-*`
    SDK_HOME="$P3_HOME/$SDK_DIR"
    MVN=$SDK_HOME/bin/$MVN
    echo "Atlassian Plugin SDK (v$SDK_VERSION) successfully installed to $SDK_HOME."
  else
    echo "Installation of the Atlassian Plugin SDK (v$SDK_VERSION) failed." >&2
    exit 1
  fi
else
  # @todo check SDK version for compatiblity
  SDK_VERSION=`atlas-version | grep "ATLAS Version" | cut -f2 -d ":" | tr -d ' '`
  echo "Using manually installed Atlassian Plugin SDK (v$SDK_VERSION)."
fi

echo "Atlassian p3 installed successfully." >&2
echo >&2
echo "Getting started:" >&2
echo >&2
echo "  1. Create an alias for the p3 script with 'alias p3=$P3_EXEC'." >&2
echo "  2. Execute 'p3 run [-d] <refapp|jira|confluence>' to start a product server." >&2
echo "  3. In a new shell, execute 'p3 create [-moc] <app key>' to create a new app." >&2
echo "  4. Use 'p3 start <app key>' to start the app." >&2
echo "  5. Visit the product-specific localhost URL and view your app's general page." >&2
echo >&2
echo "Products will run at the following urls:" >&2
echo >&2
echo "  refapp        http://localhost:5990/refapp" >&2
echo "  jira          http://localhost:2990/jira" >&2
echo "  confluence    http://localhost:1990/confluence" >&2
echo >&2
