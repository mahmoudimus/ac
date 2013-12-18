# Development Loop

The same capabilities that enable Atlassian OnDemand applications to work with Atlassian Connect add-ons exist in
installable versions of Atlassian Confluence and Atlassian JIRA as well. This means that you can use a local instance of
 the application for your initial testing and development.

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    While a local instance provides a suitable development environment for initial development and testing purposes, to try
    things out in a more realistic, production-type environment, you should use a live OnDemand instance as your target
    application and the Atlassian Marketplace.
    <p><p>
    [Managing Access Tokens](https://developer.atlassian.com/display/AC/Managing+Access+Tokens) provides more information on
    this type of testing.
</div>

The following steps describe, at a high level, the dev loop for developing Atlassian Connect add-ons. This would
typically be an iterative process, with the steps of modifying the code, deploying, and testing repeated many times.

The following steps provide more information.

## Step 1. Starting the local Atlassian application instance

The easiest way to get a local instance of the Atlassian application is by using the
[Atlassian Plugins SDK](https://developer.atlassian.com/display/DOCS/Downloads).

The SDK is the development kit used to create traditional, Java-based add-ons for the Atlassian platform. While you
don't need the project building capabilities of the SDK, you can take advantage of its features for downloading,
installing and starting Atlassian applications.

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    We recommend you start the host application using the SDK command shown here. Atlassian Connect is only present in
    Atlassian OnDemand and not yet included with Download instances of our software. Therefore certain components, including
    the Atlassian Connect Framework itself, are included here in the startup command. Without these components present,
    Connect add-ons cannot be installed. If you are not using the commands below, you must ensure all of the components
    listed in the '--bundled-plugins' argument are present in your Atlassian application.
</div>

You can start JIRA or Confluence with Atlassian Connect as follows:


### JIRA

<pre><code data-lang='none'>
atlas-run-standalone --product jira --version 6.2-OD-03 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0-m23.1,com.atlassian.jwt:jwt-plugin:1.0-m0,com.atlassian.webhooks:atlassian-webhooks-plugin:0.17.3,com.atlassian.httpclient:atlassian-httpclient-plugin:0.17.1,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.14.2-m1 --jvmargs -Datlassian.upm.on.demand=true
</pre></code>

### Confluence

<pre><code data-lang='none'>
atlas-run-standalone --product confluence --version 5.3-OD-12 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0-m23.1,com.atlassian.jwt:jwt-plugin:1.0-m0,com.atlassian.webhooks:atlassian-webhooks-plugin:0.17.3,com.atlassian.httpclient:atlassian-httpclient-plugin:0.17.1,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.14.2-m1 --jvmargs -Datlassian.upm.on.demand=true
</pre></code>

Starting the applications requires you to specify a number of hard-coded component version numbers as shown. This
includes the version of Atlassian Connect framework. Those component versions will change as Atlassian Connect
development continues. To find out about new version updates, subscribe to the Atlassian Connect
[mailing list](https://groups.google.com/forum/?fromgroups=#!forum/atlassian-connect-dev), and keep your eye on
Atlassian Connect [blog posts](https://developer.atlassian.com/display/AC/Atlassian+Connect).


## Step 2. Start your add-on host

Start your add-on host application. The options for hosting your add-on are many, but when working on a locally hosted
environment, you can use any web framework / server you wish to build your add-on.

For an example of using the simple HTTP server, see the [getting started](../guides/getting-started.html) guide.

## Step 3. Register your add-on

Registering your add-on installs it in the Atlassian application. After installation, the add-on appears in the list of
user-installed add-ons in the [Manage Add-ons](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation)
page in the administration console and its features are presented in the application UI. This mirrors the installation
process normally performed through the Marketplace for a live add-on.

You can use the UPM to install a remote Atlassian Connect add-on in your application's administration console, either
through the administration console UI of the application or using the UPM's REST API.

For more information on using the administration console, see Register the add-on in the Hello World tutorial. For
information on using the REST API, see [Installing an Add-on](../concepts/addon-installation-full.html).

## Step 4. Test your add-on

The exact steps for testing will vary based on what your add-on does, of course. However, there are some common minimal
steps applicable to any add-on for ensuring that it got installed correctly.

After registering your add-on in the Atlassian application, it should appear in the UPM as a user-installed add-on. Any
UI features it adds to the Atlassian application should now be visible as well.

To verify that your add-on installed correctly, go to the "Manage Add-ons" page in the add-on administration pages and
filter by user-installed add-ons to verify that your remote add-on appears in the list.


## Step 5. Change your code
While the Atlassian application is running, you only need to re-register the add-on when you change the descriptor file.
And you would only need to change the descriptor when modifying or adding module declarations or changing the
plugin-info details.

Other changes you make to the add-on get loaded automatically, since the application does not itself cache or otherwise
retain of the state associated with the add-on.
