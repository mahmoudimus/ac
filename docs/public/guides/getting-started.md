# Getting Started


In this Hello World tutorial, we'll turn a simple HTML page into an Atlassian Connect add-on and
install it into a local-running copy of JIRA.


## 1. Create the add-on descriptor (`atlassian-connect.json`)

An add-on descriptor is an JSON file that describes the add-on to the Atlassian application. For
example, it specifies the key and name of the add-on, lists the permissions it needs to operate, and
the different integration modules that it provides.

1. Create a project directory for your add-on source files.
2. In your project directory, create a new file named `atlassian-connect.json`.
3. Add the following text to the file:
```
    {
        "name": "Hello World",
        "description": "Atlassian Connect add-on",
        "key": "com.example.myaddon",
        "baseUrl": "http://localhost:8000",
        "vendor": {
            "name": "Example, Inc.",
            "url": "http://example.com"
        },
        "authentication": {
            "type": "none"
        },
        "version": "1.0",
        "modules": {
            "generalPages": [
                {
                    "url": "/helloworld.html",
                    "key": "hello-world",
                    "name": {
                        "value": "Greeting"
                    }
                }
            ]
        }
    }
```
4. Save and close the descriptor file.

You're now ready to create the "web app", which in our case is just a simple, old-fashioned HTML
page.

## 2. Create the web page
Now create the HTML page that serves as the add-on "web application." While a static HTML page does
not represent what would be a typical add-on, it's not that far off either. Just a few components
turn any web application into an Atlassian Connect add-on so this simple example will demonstrate
the principles.

In the same folder as the descriptor file, create a new file with a name that matches the
generalPages url attribute you set in the add-on descriptor, such as `helloworld.html`. Add the
following content:

```
<!DOCTYPE html>
<html lang="en">
    <head>
        <script src="//HOSTNAME:PORT/CONTEXT/atlassian-connect/all.js" type="text/javascript"></script>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
```

Replace these values with ones appropriate for your environment:

 * `HOSTNAME`: The hostname for the Atlassian application.
 * `PORT`: The port number on which the Atlassian application serves its web interface.
 * `CONTEXT`: The application context for the application, such as `/jira` or `/confluence`.

For this tutorial, these values will be either:

<code data-lang="text">//localhost:2990/jira/</code>

<code data-lang="text">//localhost:1990/confluence/</code>

Nothing out of the ordinary here except for one thing: the script tag for `all.js`. This JavaScript
file is a part of the Atlassian Connect library, and is available in any Atlassian application
version that supports Atlassian Connect.

The library supplies a number of functions you can use in your add-on, as described in the
[Javascript API](../concepts/javascript-api.html). For our simple HTML file, this line is required
because it enables the resizing of the iframe in which the page is to be embedded in the Atlassian
application.

<a name="start-addon-host" id="start-addon-host"></a>
## 3. Start the add-on

That's it as far as coding goes. The next step is to make the files you created available on a web
server. The options for accomplishing this are many, but this example we'll serve the file locally,
since our target application is operating locally as well.

In our case, we'll use a simple web server that ships with [Python](http://python.org) to serve the
current directory containing your `atlassian-connect.json` and `helloworld.html` files. Navigate to
that directory and run:

````
python -m SimpleHTTPServer 8000
````

After starting, the server should indicate it is serving HTTP at the current address and at the
specified port, 8000.

Confirm that you're serving the files we created in steps 1 and 2 by visiting:

<code data-lang="text"><a href="http://localhost:8000/atlassian-connect.json">http://localhost:8000/atlassian-connect.json</a></code>

<code data-lang="text"><a href="http://localhost:8000/helloworld.html">http://localhost:8000/helloworld.html</code>

## 4. Start the target Atlassian application with the Atlassian SDK

The next step in development is to start a copy of your target Atlassian application, so that you
can install your add-on.

The easiest way to get a local instance of the Atlassian application running is with the [Atlassian
SDK](https://developer.atlassian.com/display/DOCS/Downloads). If you don't have the SDK installed,
you should download and install it now.

You can start JIRA or Confluence with Atlassian Connect as follows:


#### JIRA
<pre><code data-lang="text">atlas-run-standalone --product jira --version 6.2-OD-10-004-WN --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0.0,com.atlassian.jwt:jwt-plugin:1.0.0,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.15 --jvmargs -Datlassian.upm.on.demand=true</code></pre>

#### Confluence
<pre><code data-lang="text">atlas-run-standalone --product confluence --version 5.4-OD-20-006 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0.0,com.atlassian.jwt:jwt-plugin:1.0.0,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0,com.atlassian.upm:atlassian-universal-plugin-manager-plugin:2.15 --jvmargs -Datlassian.upm.on.demand=true</code></pre>

#### Note:
We recommend you start the host application using the SDK command shown here. Atlassian Connect is
only present in Atlassian OnDemand and not yet included with Download instances of our software.
Therefore certain components, including the Atlassian Connect Framework itself, are included here in
the startup command. Without these components present, Connect add-ons cannot be installed. If you
are not using the commands below, you must ensure all of the components listed in the
`--bundled-plugins` argument are present in your Atlassian application. These component versions
will change as Atlassian Connect development continues. To find out about new version updates,
subscribe to the Atlassian Connect [mailing
list](https://groups.google.com/forum/?fromgroups=#!forum/atlassian-connect-dev), and keep your eye
on Atlassian Connect [blog posts](https://developer.atlassian.com/display/AC/Atlassian+Connect).


After the startup process completes, you can confirm that you application is running by visiting either:

<code data-lang="text"><a href="http://localhost:2990/jira/">http://localhost:2990/jira/</a></code>

<code data-lang="text"><a href="http://localhost:1990/confluence/">http://localhost:1990/confluence/</a></code>



## 5. Register your add-on in the target application

Now it's time to register your add-on with the target application.

1. Visit the target application we started in step 4, at either:
  * http://localhost:2990/jira/
  * http://localhost:1990/confluence/
1. Log in as the system administrator. The default username/password combination is admin/admin.
2. Choose __Cog Menu > Add-ons__ from the menu. The Administration page will display.
3. Choose the __Manage add-ons__ option from the side menu.
6. Click the __Upload Add-on__ link
7. Enter the URL to the hosted location of your add-on descriptor that we created in step 3. In this
example, the URL is similar to the following:
`http://localhost:8000/atlassian-connect.json`.
8. Press __Upload__. The application will display the __Installed and ready to go__ dialog when
installation is complete.
9. Click __Close__
10. Verify that your add-on appears in the list of __User installed add-ons__. For example, if you
used Hello World for your add-on name, __Hello World__ will appears in the list.

## 6. Put your add-on to work
That's it! You can now see your Hello World greeting in the Atlassian application.

1. Reload the page.
2. Look for the __Greeting__ entry in the application header (in JIRA) or the __Question Mark__
menu (in Confluence).
3. Click __Greeting__. Your __Hello World__ message appears on the page:
<img src="../assets/images/helloworld-addoninapp.jpeg" width="100%" style="border:1px solid #999;margin-top:10px;" />

## 7. What just happened?

When you register an add-on, the OnDemand instance retrieves the descriptor for the add-on
(`atlassian-connect.json`) and registers it. This adds the declared `General Page` module to the
target application and creates the "Greetings" link in the header.

When you click on the link, the `helloworld.html` file you created is fetched and rendered inside an
iframe provided by the target application.

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

## 8. What's next?

For most Atlassian Connect add-ons, the next step for the developer would be to add code that relies
on the Atlassian application REST APIs. This requires implementing the
[authentication](../concepts/authentication.html) used between Atlassian applications and Atlassian
Connect add-ons. For an overview of authentication and authorisation you may wish to read about [security](../concepts/security.html).

You can do this using any language or framework that you wish, and many languages already provide
libraries to help you with implement JWT authentication.

We've written two example implementations, one in Java and one in Javascript. These tools can help
by generating some of the plumbing required.

 * [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
 * [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express)

Also take a look at our [sample applications](../resources/samples.html). They demonstrate
authentication and many other patterns you can use to develop Atlassian Connect add-ons.


