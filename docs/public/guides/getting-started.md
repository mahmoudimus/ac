# Getting Started

In this Hello World tutorial, we'll turn an HTML page into an Atlassian Connect add-on. Using a simple HTML page allows us to focus on the things that distinguish an ordinary web application from an Atlassian Connect add-on, setting aside the details of building web applications for now.

While a deployed Atlassian Connect add-on normally operates remotely from the Atlassian OnDemand application, for this tutorial, we'll work on a single system. We'll host the add-on using a lightweight web server provided by Python. The target Atlassian application will be a local instance of an OnDemand build.

While testing locally gets you started, eventually you will need to try out the add-on in a live OnDemand instance. To do so, you'll need to create a listing for the add-on on the Marketplace. For more information, see [Listing Private Add-ons](https://developer.atlassian.com/display/AC/Listing+Private+Add-ons) and [Managing Access Tokens](https://developer.atlassian.com/display/AC/Managing+Access+Tokens). 

The command examples in this tutorial use Linux or OS X. If working on another operating system, use the command appropriate for your environment.

## Start the target Atlassian application with the Atlassian SDK

The easiest way to get a local instance of the Atlassian application running is with the [Atlassian SDK](https://developer.atlassian.com/display/DOCS/Downloads). If you don't have the SDK installed, you should download and install it now.

We'll use the `atlas-run-standalone` command, which starts an Atlassian application. The SDK lets you specify a particular version of the Atlassian application to start. For testing and developing Atlassian Connect add-ons, we're going to start the application with some specific component versions we need. 

<div class="aui-message warning">
    <p class="title">
        <span class="aui-icon icon-warning"></span>
        <strong>Important</strong>
    </p>
    We recommend you start the host application using the SDK command shown here. Atlassian Connect is only present in Atlassian OnDemand and not yet included with Download instances of our software. Therefore certain components, including the Atlassian Connect Framework itself, are included here in the startup command. Without these components present, Connect add-ons cannot be installed. If you are not using the commands below, you must ensure all of the components listed in the `--bundled-plugins` argument are present in your Atlassian application.
</div>

You can start JIRA or Confluence with Atlassian Connect as follows:

### JIRA
```
atlas-run-standalone --product jira --version 6.2-OD-05-4 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0-m25,com.atlassian.jwt:jwt-plugin:1.0-m3,com.atlassian.webhooks:atlassian-webhooks-plugin:0.17.3,com.atlassian.httpclient:atlassian-httpclient-plugin:0.17.1,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.14.2,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0 --jvmargs -Datlassian.upm.on.demand=true
```

### Confluence
```
atlas-run-standalone --product confluence --version 5.3-OD-15 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0-m25,com.atlassian.jwt:jwt-plugin:1.0-m3,com.atlassian.webhooks:atlassian-webhooks-plugin:0.17.3,com.atlassian.httpclient:atlassian-httpclient-plugin:0.17.1,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.14.2,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0 --jvmargs -Datlassian.upm.on.demand=true
```

Starting the applications requires you to specify a number of hard-coded component version numbers as shown. This includes the version of Atlassian Connect framework. Those component versions will change as Atlassian Connect development continues. To find out about new version updates, subscribe to the Atlassian Connect [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/atlassian-connect-dev), and keep your eye on Atlassian Connect [blog posts](https://developer.atlassian.com/display/AC/Atlassian+Connect).

## Create the plugin descriptor (atlassian-plugin.json)

An add-on descriptor is an JSON formatted file that describes the add-on to the Atlassian application. For example, it specifies the plugin key and name of the add-on, and lists the permissions it needs to operate.

First, set up your project and create the descriptor file for the Hello World application:

 1. Create a project directory for your add-on source files, or choose an existing directory location. 
In choosing a location, it's worth considering that you'll need to expose this directory by web server (or copy the files you create here to a location that is served by a web server).
 2. In your project directory, create a new file named `atlassian-plugin.json`.
 3. Add the following text to the file:
```
    {
        "name": "Hello World",
        "description": "Atlassian Connect add-on",
        "key": "myaddon_helloworld",
        "baseUrl": "http://localhost:8000",
        "vendor": {
            "name": "My Organization, Inc",
            "url": "https://developer.atlassian.com"
        },
        "version": "1.0",
        "modules": {
            "generalPages": [
                {
                    "url": "/helloworld.html",
                    "name": {
                        "value": "Greeting"
                    }
                }
            ]
        }
    }
```
 4. Make the following changes to the file content:
    * Change the key and name attribute values for atlassian-plugin.json to anything you like. Note that the key value must be unique for all add-ons in this instance. 
    For an add-on meant for delivery on the Atlassian Marketplace, you'll need to give it a name that won't collide with any other add-ons that the subscriber may install. The best way to do this is to qualify the key with your organization name. A common convention is to use the reverse web address for the organization, such as com.atlassian.jira.list-users.
    * Optionally, replace the description. The description and vendor information are targeted for Atlassian OnDemand instance administrators. They appear with your add-on in the administration pages for the application.
    * The `baseUrl` will need to have the hostname and port on which you are deploying the Connect add-on. Feel free to enter this now if you like, but you can also come back to this later, after starting the host (as will be described in [Start the add-on host](#start-addon-host)).

    * The `generalPages` url attribute is the URL of the resource that serves the content for our add-on page. Our resource is named `/helloworld.html`. When creating your own add-on, you would need to set this attribute value to the particular resource name appropriate for your add-on. 
 5. Save and close the descriptor file. 
You're now ready to create the "web app," which in our case is just a simple, old-fashioned HTML page.

## Create the Web page
Now create the HTML page that serves as the web application that will be our add-on. While a static HTML page does not represent what would be a typical add-on, it's not that far off either. Just a few components turn a web application into an Atlassian Connect add-on, so whether the add-on is a simple HTML page or a complete, free-standing SaaS system, the idea is the same.

In the same folder as the descriptor file, create a new file with a name that matches the generalPages url attribute you set in the add-on descriptor, such as `helloworld.html`. Add the following content:

```
<!DOCTYPE html>
<html lang="en">
    <head>
        <script src="//atlassian-app-hostname:port/context/atlassian-connect/all.js" type="text/javascript"></script>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
```

Replace these values with ones appropriate for your environment:

 * `atlassian-app-hostname`: The hostname for the Atlassian application.
 * `port`: The port number on which the Atlassian application serves its web interface.
 * `context`: The application context for the application, such as `/jira` or `/confluence`.

Nothing out of the ordinary here except for one thing: the script tag for `all.js`. This JavaScript file is a part of the Atlassian Connect library, and is available in any Atlassian application version that supports Atlassian Connect. The library supplies a number of functions you can use in your add-on, as described in the [Pages topic](https://developer.atlassian.com/display/AC/Pages#Pages-JavaScriptclientlibrary). For our simple HTML file, this line is required because it enables the resizing of the iframe in which the page is to be embedded in the Atlassian application.

<a name="start-addon-host" id="start-addon-host"></a>
## Start the add-on

That's it as far as coding goes. The next step is to make the files you created available on a web server.

The options for accomplishing this are many. The option you choose for development and initial testing depends upon whether you are testing with a live OnDemand instance or a local Atlassian application instance.

For example, if using OnDemand, you can use an existing personal website, a file hosting service that can serve static web resources, such as Bitbucket, or you can use a local server and expose it to the Web with [localtunnel](http://progrium.com/localtunnel). Any mechanism that makes the HTML file and plugin descriptor available on a web-accessible location works. For our purposes, we'll serve it locally, since our Atlassian application will be operating locally as well.

If using a POSIX-based operating system and you have Python, you can use it to serve up your project directory as a static web server. For other operating systems or setups, you can use a web server like Apache or Nginx.

In our case, we'll use Python to serve the current directory:
```
python -m SimpleHTTPServer 8000
```

After starting, the server should indicate it is serving HTTP at the current address and at the specified port, 8000.

It's a good idea to double-check the URL you specified in the descriptor after you start the add-on host. In a browser, make sure you can access the URL formed by the `baseUrl`, concatenated with the generalPage URL.

For example:
```
http://localhost:8000/helloworld.html
```

If needed, change the values in the descriptor to reflect the actual location of the resource now. That is, the url attribute values in these lines of the `atlassian-plugin.json` file:

```
{
    "name": "Hello World",
    "baseUrl": "http://localhost:8000",
    "modules": {
        "generalPages": [
            {
                "url": "/helloworld.html",
                "name": {
                    "value": "Greeting"
                }
            }
        ]
    }
}
```

## Install the add-on
Installing your add-on adds it to your OnDemand application. To be more precise, installing is really just registering the add-on with the application and the only thing that is stored by the application at this time is the add-on descriptor. 

You can install an add-on with the UPM as follows. Note, these instructions were written for UPM version 2.14 or later.

 1. Log in to the Atlassian application interface as an admin or a system administrator. If you started the application with Atlassian's SDK, the  default username/password combination is admin/admin
 2. Choose <img src="../assets/images/cog.png" alt="Settings" /> > __Add-ons__ from the menu. The Administration page will display
 3. Choose the __Manage add-ons__ option
 4. Scroll to the page's bottom and click the __Settings__ link. The __Settings__ dialog will display
 5. Make sure the "Private listings" option is checked and click __Apply__
 6. Scroll to the top of the page and click the __Upload Add-on__ link
 7. Enter the URL to the hosted location of your plugin descriptor. In this example, the URL is similar to the following:  `http://localhost:8000/atlassian-connect.json`. (If you are installing to an OnDemand instance,
 the URL must be served from the Marketplace, [such as this example](https://marketplace.atlassian.com/download/plugins/com.example.add-on/version/39/descriptor?access-token=9ad5037b))
 8. Press __Upload__. The system takes a moment to upload and register your plugin. It displays the __Installed and ready to go__ dialog when installation is complete. <img width="100%" src="../assets/images/installsuccess.jpeg" />
 9. Click __Close__
 10. Verify that your plugin appears in the list of __User installed add-ons__. For example, if you used Hello World for your plugin name, that will appears in the list

## Put your add-on to work
That's it! You can now see your Hello World greeting in the Atlassian application.

 1. Scroll to the top of the page and look for the __Greeting__ entry in the application header.
If you don't immediately see the entry, reload your page.
 2. Click __Greeting__. <br /> Your __Hello World__ message appears on the page: <img src="../assets/images/helloworld-addoninapp.jpeg" width="100%" />

## What just happened?

When you register an add-on, the OnDemand instance retrieves the descriptor for the add-on (`atlassian-plugin.json`) and installs it. This adds the add-on to the application's interface, including a link in the application's header.

When you click on the link, your page is rendered inside an iframe wrapped by the application's header and footer.

<div class="diagram">
participant User
participant Browser
participant Add_on_server
participant OnDemand
User->OnDemand: View your Hello World page
OnDemand->Browser:OnDemand sends back page\nwith iframe to your addon 
Browser->Add_on_server:GET /helloworld.html?signed_request=* 
Add_on_server->Browser:Responds with contents of\n helloworld.html page 
Browser->User:Requested page\nrendered
</div>

## What's next?

While not particularly useful in itself, the Hello World add-on illustrates how to go about building your own add-on.

For most Atlassian Connect add-ons, the next step for the developer would be to add code that relies on the Atlassian application REST APIs. This involves implementing the authentication mechanism used between Atlassian applications and Atlassian Connect add-ons, JWT.

The implementation details for JWT vary considerably depending on the programming language and framework you are using to develop your add-on.

This is where our framework helpers come in. They help you build some of the plumbing required between Atlassian Connect add-ons and OnDemand applications.

 * [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
 * [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express)

Also take a look at our [sample applications](../resources/samples.html). They demonstrate authentication and many other patterns you can use to develop Atlassian Connect add-ons.
