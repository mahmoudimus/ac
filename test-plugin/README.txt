Standalone test Atlassian Remotable Plugin

This is a standalone, test remote plugin used for testing Remotable Plugins features.
While it relies on Java and Maven, Remotable Plugins aren't restricted to any
particular language, platform, or library.

To get started, build the app:
  mvn package
  
To run the app, execute the standalone jar:
  java -jar target/remotable-plugins-test-plugin-VERSION-standalone.jar APP_KEY HOST_BASE_URL APP_BASE_URL PORT

For example, this command would run the app on port 5432, proxied as https://mydomain.com to the 
world, to be embedded into the remoteapps.jira.com JIRA instance:
  java -jar target/remotable-plugins-test-plugin-VERSION-standalone.jar myAppKey https://remoteapps.jira.com https://mydomain.com 5432

Then, to install the Remotable Plugin via the "Extensions" user profile page (Speakeasy), enter the URL:
  https://mydomain.com/register
