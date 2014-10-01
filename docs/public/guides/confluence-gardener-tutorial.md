##Tutorial: Manage your Confluence instance

<div class="aui-message">
	    <p class="title">
	        <span class="aui-icon icon-info"></span>
	        <strong>Who this tutorial is for</strong>
	    </p>
	    <p>
	    You can complete this tutorial even if you've never built an Atlassian add-on before. You'll
	    need at least version 4.2.20 of the [Atlassian SDK installed](https://developer.atlassian.com/display/DOCS/Downloads).
	    </p>
</div>

In this tutorial, you'll learn about:

* [Configuring your development environment](#environment)
* [Hosting your add-on locally](#hosting-locally)
* [Implement a Confluence REST API client](#rest-api)
* [Display a full-screen dialog](#dialog)

This tutorial will show you how to build a static Atlassian Connect add-on which displays a hierarchy of pages in a
Confluence Space.

Your add-on will use the [Confluence REST API](https://docs.atlassian.com/confluence/REST/latest/) to query, update and
delete Confluence pages. It will also show you how to create a full-screen confirmation dialog displaying content
from your add-on.

When you're finished, your add-on will look similar to this:

<img src="../assets/images/confluence-gardener-screen.png" width="100%" style="border:1px solid #999;margin-top:10px;" />

## <a name="environment"></a> Configuring your development environment

In this step you'll ensure you have git and the Atlassian SDK installed and clone the Confluence Gardener git repository.

### __Install git__

If you have git installed you can skip this step.

Git is a wildly popular version control system which you'll need to complete this tutorial.
If you don't yet have git installed, you can find the
<a href="http://git-scm.com/book/en/Getting-Started-Installing-Git" target="_blank">installation instructions for your system here</a>.

### __Install Node.js__

If you have Node.js installed you can skip this step.

We'll be using a Node.js powered static webserver in this tutorial.
If you don't yet have Node.js installed, you can find the
<a href="https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager" target="_blank">installation instructions for your system here</a>.

### __Install the Atlassian SDK__

If you have the version 4.2.20 or higher of the Atlassian SDK installed you can skip this step.

The Atlassian Plugin SDK provides you with a toolkit of commands which are vital for developing Atlassian plugins.
To run Confluence Gardener, we'll be using a single command, [`atlas-run-standalone`](https://developer.atlassian.com/display/DOCS/atlas-run-standalone), which runs a copy of an Atlassian product on your computer.

Use the following links to install the SDK

* [Instructions for Windows users](https://developer.atlassian.com/display/DOCS/Install+the+Atlassian+SDK+on+a+Windows+System)
* [Instructions for Mac/Linux users](https://developer.atlassian.com/display/DOCS/Install+the+Atlassian+SDK+on+a+Linux+or+Mac+System)

Confirm the SDK is installed by running `atlas-version`.

<pre><code data-lang="text">$ atlas-version</code></pre>

You should see 4.2.20 or higher:

<pre>
ATLAS Version:    4.2.20
ATLAS Home:       /usr/share/atlassian-plugin-sdk-4.2.20
ATLAS Scripts:    /usr/share/atlassian-plugin-sdk-4.2.20/bin
ATLAS Maven Home: /usr/share/atlassian-plugin-sdk-4.2.20/apache-maven
</pre>

### __Clone the Confluence Gardener repository__

At the command line, enter the following git command to clone the Confluence Gardener repository.
<pre><code data-lang="text">$ git clone https://bitbucket.org/atlassianlabs/confluence-gardener.git</code></pre>

Next change directories into the newly created `confluence-gardener` directory.
<pre><code data-lang="text">$ cd confluence-gardener</code></pre>

### __Start Confluence in cloud mode from your `confluence-gardener` directory__

<span id="commands-confluence-prd">Loading...</span>

You'll see a lot of output. When finished, your terminal will notify you that Confluence was successfully started:

<pre>
[INFO] [talledLocalContainer] Tomcat 6.x started on port [1990]
[INFO] Confluence started successfully in 116s at http://localhost:1990/confluence
[INFO] Type Ctrl-D to shutdown gracefully
[INFO] Type Ctrl-C to exit
</pre>

### __Log in__

In your browser load the URL displayed at the end of the previous step. In this case, http://localhost:1990/confluence
and login with the following credentials:

__Username__: `admin`

__Password__: `admin`

## <a name="hosting-locally"></a> Hosting your add-on locally

Confluence Gardener is a static Atlassian Connect add-on and can be hosted with a simple static web server.

We'll use a simple Node.js powered web server to host your add-on locally.

1. From the `confluence-gardener` directory, start your server on port 8000:
    <pre><code data-lang="text">
    npm install -g http-server
   http-server -p 8000
    </code></pre>
    The server indicates that it's serving HTTP at the current address and port. You'll see something like:
    <tt>Starting up http-server, serving ./ on: http://0.0.0.0:8000</tt>

1. Confirm your server is running by loading http://localhost:8000/atlassian-connect.json in your browser.

    The result should begin with:

    <pre><code data-lang="javascript">
    {
        key: "confluence-gardener",
        name: "Confluence Gardener",
        description: "Prune back your Confluence page graph.",
        baseUrl: "http://localhost:8000",
        ...
    </code></pre>

## <a name="installing"></a> Installing the add-on

We've provided you with a file called  `install-confluence-gardener.sh`. If you're able to run Bash scripts, run it by entering the
following command from the Confluence Gardener directory to install the add-on.

<pre><code data-lang="text">$ ./install-confluence-gardener.sh</code></pre>

Alternatively you can install the add-on using the Universal Plugin Manager (UPM).

<div id="upm-instructions" class="aui-expander-content">
    <h3>Set the Confluence Base URL</h3>

    <ul>
        <li>Navigate to http://localhost:1990/confluence/admin/editgeneralconfig.action</li>
        <li>Set the field `Server Base URL` to `http://localhost:1990/confluence` if it is not already.</li>
        <li>Click save.</li>
    </ul>

    <h3>Install the plugin</h3>

    <ul>
        <li>Navigate to http://localhost:1990/confluence/plugins/servlet/upm</li>
        <li>Click Upload add-on.</li>
        <li>Enter http://localhost:8000/atlassian-connect.json</li>
        <li>Click `Upload`</li>
    </ul>
</div>

<a data-replace-text="Hide UPM installation instructions [-]" class="aui-expander-trigger" aria-controls="upm-instructions">Expand UPM installation instructions [+]</a>

Due to some missing functions we're going to implement below, Confluence Gardener won't work yet, but feel free to try
to load it anyway.

1. Load the Demonstration Space by browsing to `http://localhost:1990/confluence/display/ds`
1. Click the Tools menu at the top right and click 'Confluence Gardener'

Let's implement those missing functions now.

## <a name="rest-api"></a> Implement a Confluence REST API client

All of the functions which request data from Confluence are contained in the file `js/data.js`. Open this file in
the editor of your choice. Oh no! The functions are incomplete. Let's go ahead and implement them.

<pre><code data-lang="javascript">
define(function() {
    return {
        getPageContentHierarchy: function(pageId, callback) {
        },

        getSpaceHierarchy: function(spaceKey, callback) {
        },

        removePage: function(pageId, callback) {
        },

        movePage: function(pageId, newParentId, callback) {
        },

        movePageToTopLevel: function(pageId, spaceKey, callback) {
        }
    }
});
</code></pre>

Using the [Confluence REST API browser](https://bunjil.jira-dev.com/wiki/plugins/servlet/restbrowser) and
the [AP.request documentation](/static/connect/docs/javascript/module-request.html), try to implement these functions.

At minimum, implement `getPageContentHierarchy` and `getSpaceHierarchy` and refresh Confluence Gardener to display the
hierarchy of pages in the Demonstration Space.

If you're having trouble getting the it to work feel free to check out a working implementation below.

### `data.js` working implementation

<a data-replace-text="Hide working implementation [-]" class="aui-expander-trigger" aria-controls="working-data-implementation">Show working implementation [+]</a>

<div id="working-data-implementation" class="aui-expander-content">
<pre><code data-lang="javascript">
define(function() {
    return {
        getPageContentHierarchy: function(pageId, callback) {
            AP.request({
                url: "/rest/prototype/1/content/" + pageId + ".json?expand=children",
                success: callback
            });
        },

        getSpaceHierarchy: function(spaceKey, callback) {
            AP.request({
                url: "/rest/prototype/1/space/" + spaceKey + ".json?expand=rootpages",
                success: callback
            });
        },

        removePage: function(pageId, callback) {
            AP.request({
                url: "/rpc/json-rpc/confluenceservice-v2/removePage",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify([pageId]),
                success: callback
            });
        },

        movePage: function (pageId, newParentId, callback) {
            AP.request({
                url: "/rpc/json-rpc/confluenceservice-v2/movePage",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify([pageId, newParentId, "append"]),
                success: callback
            });
        },

        movePageToTopLevel: function(pageId, spaceKey, callback) {
            AP.request({
                url: "/rpc/json-rpc/confluenceservice-v2/movePageToTopLevel",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify([pageId, spaceKey]),
                success: callback
            });
        }
    }
});
</code></pre>
</div>

## <a name="dialog"></a> Display a full-screen dialog

Having implemented all of the REST Client functions, you can now feel free to click around and explore Confluence
 Gardener. However you may have noticed that the remove button doesn't work. Let's make it display [a full-screen
 dialog](https://developer.atlassian.com/static/connect/docs/javascript/module-Dialog.html) now.

<img src="../assets/images/confluence-gardener-screen-2.png" width="100%" style="border:1px solid #999;margin-top:10px;" />

### Declare the Dialog in the atlassian-connect.json descriptor file.

Full-screen dialogs are fully-fledged AUI Dialogs which exist in the parent frame (that is, not in the iframe
Confluence Gardener displays in). This AUI Dialog itself contains an iframe which refers back to a web page your add-on
hosts. We've created the HTML of that page for you, but we need to reference it in the atlassian-connect.json
descriptor.

In your editor, open `atlassian-connect.json` and add the following snippet after the `generalPages` entry
in the `modules` object and re-install the add-on using [the method you used above](#installing).

This registers a new [`webItem`](https://developer.atlassian.com/static/connect/docs/modules/jira/web-item.html) which
  will load inside the full-screen Dialog.

<pre><code data-language="javascript">
"webItems": [
    {
        "key": "gardener-remove-dialog",
        "url": "/remove-page-dialog.html",
        "location": "system.top.navigation.bar",
        "weight": 200,
        "context": "addon",
        "target": {
            "type": "dialog",
            "options": {
                "width": "234px",
                "height": "324px"
            }
        },
        "name": {
            "value": "dialog"
        }
    }
]
</code></pre>

<a data-replace-text="Hide atlassian-connect.json [-]" class="aui-expander-trigger" aria-controls="complete-descriptor">Show atlassian-connect.json [+]</a> with the dialog `webItems` entry.

<div id="complete-descriptor" class="aui-expander-content">
<pre><code data-language="javascript">
{
    "key": "confluence-gardener",
    "name": "Confluence Gardener",
    "description": "Prune back your Confluence page graph.",
    "baseUrl": "http://localhost:8000",
    "vendor": {
        "name": "Atlassian Labs",
        "url": "https://www.atlassian.com"
    },
    "authentication": {
        "type": "none"
    },
    "version": "0.1",
    "modules": {
    "generalPages": [
         {
             "key": "gardener",
             "url": "/index.html?spaceKey={space.key}",
             "location": "system.content.action",
             "name": {
                 "value": "Confluence Gardener"
             }
         }
     ],
     "webItems": [
         {
             "key": "gardener-remove-dialog",
             "url": "/remove-page-dialog.html",
             "location": "system.top.navigation.bar",
             "weight": 200,
             "context": "addon",
             "target": {
                 "type": "dialog",
                 "options": {
                     "width": "234px",
                     "height": "324px"
                 }
             },
             "name": {
                 "value": "dialog"
             }
         }
     ]
    },
    "scopes": [
        "read",
        "write",
        "delete"
    ]
}
</code></pre></div>

### Implement a function to display the Remove page dialog

Open the `removePageDialog.js` file in your editor. You will see another empty function:

<pre><code data-lang="javascript">
define(function() {
    return function (deleteCallback) {
    }
});
</code></pre>

The returned anonymous function performs needs to perform three small tasks:

1. Load the [`dialog`](https://developer.atlassian.com/static/connect/docs/javascript/module-Dialog.html)
and [`events`](https://developer.atlassian.com/static/connect/docs/javascript/module-Events.html) modules using `AP.require`.

1. Unsubscribe from any previous `confirmPageRemoval` event bindings
1. Subscribe to the `confirmPageRemoval` using the provided `deleteCallback` function as the listener.

Go ahead and try to implement the function. If you get stuck you can expand a working implementation below.

### Working implementation of `removePageDialog.js`

<a data-replace-text="Hide removePageDialog.js [-]" class="aui-expander-trigger" aria-controls="complete-remove-dialog">Show removePageDialog.js [+]</a>

<div id="complete-remove-dialog" class="aui-expander-content">
<pre><code data-lang="javascript">
define(function() {
    return function(deleteCallback) {
        AP.require(["dialog", "events"], function (dialog, events) {
            dialog.create({
                key: 'gardener-remove-dialog',
                width: "400px",
                height: "80px",
                header: "Remove page?",
                submitText: "Remove",
                cancelText: "cancel",
                chrome: true
            });

            events.offAll("confirmPageRemoval");
            events.on("confirmPageRemoval", deleteCallback);
        });
    }
});
</code></pre></div>

That's it! Feel free to play around with the other portions of Confluence Gardener.