# Getting Started

Add-ons are essentially web applications that integrate with your Atlassian applications. In this tutorial, you'll turn a simple HTML page (a web application, for the sake of illustration) into an Atlassian Connect add-on. You'll install it in a local running copy of JIRA in OnDemand mode. This tutorial is meant to show you a very basic preview of the Connect framework, and how it interacts with Atlassian applications. By completion, you will: 

* [Create a basic `atlassian-connect.json` descriptor](#descriptor)  
* [Build an add-on web application: A super simple HTML page](#webapp)  
* [Start up a locally-running copy of JIRA in OnDemand mode](#runjira)  
* [Install and test your add-on](#install)

By the end, you'll see something like this:  

<img src="../assets/images/helloworld-addoninapp.jpeg" width="80%" style="border:1px solid #999;margin-top:10px;" />

__Note__: You can complete this tutorial even if you've never built an add-on before. 

You'll need the [Atlassian SDK](https://developer.atlassian.com/display/DOCS/Downloads) installed and ready to go.  


## <a name="descriptor"></a>Create the add-on descriptor (`atlassian-connect.json`)

In this step you'll create a JSON descriptor file. This file describes the add-on to the Atlassian application, which is JIRA in this tutorial. Your descriptor specifies your add-on's key, name, permissions needed to operate, and the different modules it uses for integration. 

Your `atlassian-connect.json` file will use a `generalPages` module, and add a link to JIRA's top navigation element titled "Your excellent add-on".

1. Create a project directory for your add-on source files.
    <pre><code data-lang="text">
        mkdir connect && cd connect/
    </code></pre>
    You'll work in this directory for the duration of this tutorial.
2. In your project directory, create a new file named `atlassian-connect.json`.
    <pre><code data-lang="text">vi atlassian-connect.json</code></pre>
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

    You can click __ESC + `:wq`__ to exit and save the file.

## <a name="webapp"></a>Create a simple web application to stand in as an add-on

Now, you're ready to create the web app. You'll use a simple, old-fashioned HTML page as an "app" to demonstrate how Connect integrates with your application. While a static HTML page doesn't represent a typical add-on, it's not that far off either. Just a few components turn any web application into an Atlassian Connect add-on.

You'll add two key elements in a single HTML file: a `script src` element, and an `ac-content` wrapper class. 

<table class="aui">
    <thead>
        <tr>
            <th>Element</th>
            <th>Details</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>__`script src`__</td>
            <td>
                <p>
                This element is comprised of 3 values pointing toward `all.js`, formatted as `"//HOSTNAME:PORT/CONTEXT/atlassian-connect/all.js"`. Let's look at the components:
                </p>
                <ul>
                    <li>`HOSTNAME`: The hostname for the Atlassian application. Here, we use `localhost` for the sake of simplicity.</li>
                    <li>`PORT`: The port number on which the Atlassian application serves its web interface. JIRA uses port 2990, and Confluence uses 1990.</li>
                    <li>`CONTEXT`: The application context for the application, such as `/jira` or `/confluence`.</li>
                    <li>`all.js`: This file is available in any Atlassian application that supports Connect. This [Javascript API library](../concepts/javascript-api.html) provides functions you can use for your add-on. In this case, it enables iframe resizing for the JIRA page that displays your add-on.</li>
        </tr>
        <tr>
            <td>__`ac-content`__</td>
            <td>This class wraps the content of your add-on, and dynamically resizes the iframe in JIRA. This keeps your add-on content visible without pesky scrollbars.</td>
        </tr>
    </tbody>
</table>  

Make sense? Let's get started.  

1. In the same directory, create the page you referenced in the `url` element in your descriptor file, `helloworld.html`.
    <pre><code data-lang="text">vi helloworld.html</code></pre>
2. Add the following content:

    ```
<!DOCTYPE html>
<html lang="en">
    <head>
        <script src="//localhost:2990/jira/atlassian-connect/all.js" type="text/javascript"></script>
    </head>
    <body>
        <div class="ac-content">
            <h1>Hello world</h1>
            <img src="http://www.placebear.com/500/500"/>
        </div>
    </body>
</html>
```  


## <a name="start-addon-host" id="start-addon-host"></a> Start your add-on

That's it as far as coding goes. The next step is to make the files you created available on a web
server. The options for accomplishing this are many, but this example we'll serve the file locally,
since our target application is operating locally as well.

In our case, we'll use a simple web server that ships with [Python](http://python.org) to serve the
current directory containing your `atlassian-connect.json` and `helloworld.html` files. 

1. From the same directory, start your server on port 8000:
     <pre><code data-lang="text">python -m SimpleHTTPServer 8000</code></pre>
    The server indicates that it's serving HTTP at the current address and port. You'll see something like this: 
    <tt>Serving HTTP on 0.0.0.0 port 8000 ...</tt> 
2. Confirm the files you created in steps 1 and 2 are served. Visit:
    * <code data-lang="text"><a href="http://localhost:8000/atlassian-connect.json">http://localhost:8000/atlassian-connect.json</a></code>
    * <code data-lang="text"><a href="http://localhost:8000/helloworld.html">http://localhost:8000/helloworld.html</a></code>

## <a name="runjira"></a>Start JIRA using the Atlassian SDK

You've created the essential components of a Connect add-on: You have an `atlassian-connect.json` descriptor file to communicate what your add-on does to JIRA, and a web application (`helloworld.html`) running on a local server. Now, you need to start up JIRA to install your add-on. 

You'll start JIRA in OnDemand mode. Connect is only present in OnDemand (cloud instances) of Atlassian products, and not yet included with downloaded or locally-hosted instances. For this reason, certain components like the Connect framework itself, are included in startup commands. Without these components Connect add-ons aren't installable. 

1. Ensure you have the [Atlassian SDK installed](https://developer.atlassian.com/display/DOCS/Downloads).  
    <pre><code data-lang="text">$ atlas-version</code></pre>
    You should see something like this:  

    <tt>
        ATLAS Version:    4.2.20  
        ATLAS Home:       /usr/share/atlassian-plugin-sdk-4.2.20  
        ATLAS Scripts:    /usr/share/atlassian-plugin-sdk-4.2.20/bin  
        ATLAS Maven Home: /usr/share/atlassian-plugin-sdk-4.2.20/apache-maven
        ...
    </tt>
  
2. From a new terminal window, start JIRA in OnDemand mode: 
    <pre><code data-lang="text">atlas-run-standalone --product jira --version 6.3-OD-03-012 --bundled-plugins com.atlassian.plugins:atlassian-connect-plugin:1.0.2,com.atlassian.jwt:jwt-plugin:1.0.0,com.atlassian.bundles:json-schema-validator-atlassian-bundle:1.0-m0 --jvmargs -Datlassian.upm.on.demand=true</code></pre>
    __Note:__ If you're not using the command above, ensure all components in the `--bundled-plugins` argument are present in your JIRA instances. These component versions will change as Connect development continues.  
    
    You'll see a lot of output. When finished, your terminal notifies you that the build was successful:  
    <tt>[INFO] [talledLocalContainer] Tomcat 7.x started on port [2990]  
        [INFO] jira started successfully in 217s at http://localhost:2990/jira  
        [INFO] Type Ctrl-D to shutdown gracefully  
        [INFO] Type Ctrl-C to exit
    </tt>  

3. Navigate to <code data-lang="text"><a href="http://localhost:2990/jira/">http://localhost:2990/jira/</a></code>.
4. Sign in with `admin` for your username, and `admin` for your password.


## <a name="install"></a>Install your add-on in JIRA

This step is straightforward if you've ever used the [Universal Plugin Manager (UPM)](https://confluence.atlassian.com/x/8AJTE) before. You'll navigate to the admin section, and add a link to your descriptor file.

When you install your add-on, JIRA retrieves and registers your `atlassian-connect.json` descriptor. The dance between JIRA and your web app (your add-on) looks a bit like this: 

<div class="diagram">
participant User
participant Browser
participant Add_on_server
participant JIRA
User->JIRA: Click 'Greeting'
JIRA->Browser:JIRA sends back your \nadd-on in an iframe
Browser->Add_on_server:GET /helloworld.html?signed_request=*
Add_on_server->Browser:Responds with contents of\n helloworld.html page
Browser->User:Requested page\nrendered
</div>

1. From JIRA, choose __Cog Menu > Add-ons__ from the top navigation menu. 

    __Note:__ If you see a message about [base URL problems](https://confluence.atlassian.com/x/FgNTE), choose __System > General Configuration > Edit Configuration__ from the admin screen. Ensure your browser URL, base URL, and `helloworld.html` hostname all match.
    
2. Click __Manage add-ons__ from the side menu. 

3. Click __Upload Add-on__ from the right side of the page.

4. Insert `http://localhost:8000/atlassian-connect.json`.  
     This URL should match the hosted location of your `atlassian-connect.json` descriptor file.

5. Click __Upload__.  
    JIRA displays the *Installed and ready to go* dialog when installation is complete.
    
6. Click __Close__.

7. Verify that your add-on appears in the list of *User installed add-ons*.   
    For example, if you used Hello World for your add-on name, *Hello World* should appear in the list.
    
8. Reload the page.

9. Click __Greeting__ in the application header.  
    Your message appears on the page:  
<img src="../assets/images/helloworld-addoninapp.jpeg" width="100%" style="border:1px solid #999;margin-top:10px;" />


## 8. What's next?

For most Atlassian Connect add-ons, the next step is to add code that relies
on the Atlassian application REST APIs. This requires implementing the
[authentication](../concepts/authentication.html) used between Atlassian applications and Atlassian
Connect add-ons. For an overview of authentication and authorization, take a look at our [security concepts](../concepts/security.html).

You can do this using any language or framework that you want, and many languages already provide
libraries to help you with implement JWT authentication.

We've written two example implementations, one in Java and one in Javascript. These tools can help
by generating some of the plumbing required.

 * [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
 * [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express)

Also, take a look at our [sample applications](../resources/samples.html). These samples demonstrate
authentication and many other patterns you can use to develop your own Connect add-ons.

##Tabled for now
To find out about new version updates,
subscribe to the Atlassian Connect [mailing
list](https://groups.google.com/forum/?fromgroups=#!forum/atlassian-connect-dev), and keep your eye
on Atlassian Connect [blog posts](https://developer.atlassian.com/display/AC/Atlassian+Connect).
