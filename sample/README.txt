Standalone sample Atlassian Remote App

This is a standalone, sample remote app for those interested in developing
their own Remote App.  While it relies on Java and Maven, Remote Apps
aren't restricted to any particular language, platform, or library.

To get started, build the app:
  mvn package
  
To run the app, execute the standalone jar:
  java -jar target/remoteapps-sample-VERSION-standalone.jar APP_KEY HOST_BASE_URL APP_BASE_URL

For example, this command would run the app to be embedded into a local JIRA instance:
  java -jar target/remoteapps-sample-VERSION-standalone.jar myAppKey http://localhost:2990/jira http://localhost:5432

Then, to install the Remote App via the "Extensions" user profile page (Speakeasy), enter the URL:
  http://localhost:5432/user-register
