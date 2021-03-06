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

This tutorial shows you how to build a static Connect add-on that displays page hierarchy in a Confluence space. This 
add-on is handy to query, update, and delete Confluence pages. You'll also create a full-screen confirmation dialog
displaying content from your add-on.  

Your add-on will use the <a href="https://docs.atlassian.com/confluence/REST/latest/" target="_blank">
Confluence REST API</a>. At completion, your add-on will look a lot like this: 

<img src="../assets/images/confluence-gardener-screen.png" width="80%" style="border:1px solid #999;margin-top:10px;" />

## <a name="environment"></a> Configuring your development environment

This step confirms your development environment is configured correctly. You'll need <a href="http://git-scm.com/" 
target="_blank">Git</a>, <a href="http://nodejs.org/" target="_blank">Node.js</a>, and your
[Atlassian add-on development environment configured](#environment).

The Atlassian Plugin SDK provides a command toolkit vital for developing Atlassian add-ons.
To run Confluence Gardener, you'll use the
[`atlas-run-standalone`](https://developer.atlassian.com/display/DOCS/atlas-run-standalone) command, which starts a
version of an Atlassian product tuned for development.

Once you have all the prerequisites, you'll clone an existing repository to kick things off.  

<a data-replace-text="Hide Git installation instructions [-]" class="aui-expander-trigger" aria-controls="install-git">Show Git installation instructions [+]</a>

<div id="install-git" class="aui-expander-content">
    <span data-include="../assets/includes/install-git.html"></span>
</div>

<a data-replace-text="Hide Node.js instructions [-]" class="aui-expander-trigger" aria-controls="install-node">Show Node.js instructions [+]</a>

<div id="install-node" class="aui-expander-content">
    <span data-include="../assets/includes/install-nodejs.html"></span>
</div>

<a data-replace-text="Hide SDK instructions [-]" class="aui-expander-trigger" aria-controls="install-sdk">Show SDK instructions [+]</a>

<div id="install-sdk" class="aui-expander-content">
    <span data-include="../assets/includes/install-atlassian-sdk.html"></span>
</div>

1. Clone the Confluence Gardener repository. 
    <pre><code data-lang="text">$ git clone https://bitbucket.org/atlassianlabs/confluence-gardener.git</code></pre>
1. Change into your new `confluence-gardener` directory.  
    <pre><code data-lang="text">$ cd confluence-gardener</code></pre>
1. Start Confluence in cloud mode from your `confluence-gardener` directory. 
    <span id="commands-confluence-prd">Loading...</span>
    You'll see a lot of output. When finished, your terminal will notify you that Confluence started successfully:
    <pre>
    [INFO] [talledLocalContainer] Tomcat 6.x started on port [1990]
    [INFO] Confluence started successfully in 116s at http://localhost:1990/confluence
    [INFO] Type Ctrl-D to shutdown gracefully
    [INFO] Type Ctrl-C to exit
    </pre>
1. Navigate in your browser to your fresh Confluence instance, usually at <a href="http://localhost:1990/confluence" target="_blank">http://localhost:1990/confluence</a>. 
1. Log in as an administrator:   
    __Username__: `admin`  
    __Password__: `admin`    


## <a name="hosting-locally"></a> Host & install your add-on

Confluence Gardener is a static Connect add-on that can be served by a simple web server and cached with a CDN.
We'll use a simple Node.js-powered static web server to host your add-on locally.

After you've spun up your server, you'll install your copy of Gardener to Confluence. You'll use a Bash script 
included in the repo you cloned to install the add-on.   

1. From the `confluence-gardener` directory, start your server on port 8000:
    <span data-include="../assets/includes/start-http-server.html"></span>

1. In your browser, navigate to your descriptor file at <a href="http://localhost:8000/atlassian-connect.json" 
    target="_blank">http://localhost:8000/atlassian-connect.json</a>  

    <a data-replace-text="Hide atlassian-connect.json [-]" class="aui-expander-trigger" aria-controls="complete-descriptor">Show atlassian-connect.json [+]</a>

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
             ]
        },
        "scopes": [
            "read",
            "write",
            "delete"
        ]
    }
    </code></pre></div>
1. In a new terminal window, run the `install-confluence-gardener.sh` script. 
    <pre><code data-lang="text">$ ./install-confluence-gardener.sh</code></pre>  
    Alternatively, you can install Gardener using the <a href="https://confluence.atlassian.com/x/8AJTE" target="_blank">Universal Plugin Manger (UPM)</a>.  
    <a data-replace-text="Hide UPM installation instructions [-]" class="aui-expander-trigger" aria-controls="upm-instructions">Expand UPM installation instructions [+]</a>

    <div id="upm-instructions" class="aui-expander-content">
        <h3>Set the Confluence Base URL</h3>

        <ul>
            <li>Navigate to <a href="http://localhost:1990/confluence/admin/editgeneralconfig.action" target="_blank">http://localhost:1990/confluence/admin/editgeneralconfig.action</a>.</li>
            <li>Set your `Server Base URL` field to `http://localhost:1990/confluence`.</li>
            <li>Click <b>Save</b>.</li>
        </ul>

        <h3>Install Gardener</h3>

        <ul>
            <li>Navigate to http://localhost:1990/confluence/plugins/servlet/upm</li>
            <li>Click <b>Upload add-on</b>.</li>
            <li>Enter <tt>http://localhost:8000/atlassian-connect.json</tt></li>
            <li>Click <b>Upload</b>.</li>
        </ul>
    </div>

    Gardener doesn't have functionality yet (you'll implement that in future steps), but feel free to try to load it anyway. 
1.  Browse to the Demonstration space at <a href="http://localhost:1990/confluence/display/ds" 
    target="_blank"> http://localhost:1990/confluence/display/ds</a>.
1. Click <b>Tools</b> at the top right, and choose <b>Confluence Gardener</b>.  
    You should see a page like this: 
    <img src="../assets/images/confluence-gardener-0.png" width="80%" style="border:1px solid #999;margin-top:10px;" />

Now you're ready to start developing functionality for your Gardener add-on. 

## <a name="rest-api"></a> Implement a Confluence REST API client

All the functions that request data from Confluence are in your `js/data.js` file. The functions are incomplete, 
so in this step you'll flesh these out. 

You'll use the [REST API browser](../rest-apis/product-api-browser.html) and
the [AP.request documentation](../javascript/module-request.html) to implement functions to get
page and space hierarchy in Confluence, and add Gardener functionality to move and remove pages.

1. Open the `js/data.js` file from your `confluence-gardener` source code.
    You should see the stub code below:  
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
1. Implement `getPageContentHierarchy` to get the page hierachy in your Confluence instance:
    <pre><code data-lang="javascript">
    getPageContentHierarchy: function(pageId, callback) {
        AP.request({
            url: "/rest/prototype/1/content/" + pageId + ".json?expand=children",
            success: callback
        });
    },
    </code></pre>
1.  Next, implement `getSpaceHierarchy` to see the space hierarchy: 
    <pre><code data-lang="javascript">
    getSpaceHierarchy: function(spaceKey, callback) {
        AP.request({
            url: "/rest/prototype/1/space/" + spaceKey + ".json?expand=rootpages",
            success: callback
        });
    },
    </code></pre>  
1. Implement `removePage` so your add-on can effectively delete Confluence pages: 
    <pre><code data-lang="javascript">
    removePage: function(pageId, callback) {
        AP.request({
            url: "/rpc/json-rpc/confluenceservice-v2/removePage",
            contentType: "application/json",
            type: "POST",
            data: JSON.stringify([pageId]),
            success: callback
        });
    },
    </code></pre>
1. Finally, try to implement `movePage` and `movePageToTopLevel` on your own. 
    If you get stuck, expand the example below.  

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
1. Now, refresh Gardener in your browser.  
    You should see a page like this:  
    <img src="../assets/images/confluence-gardener-.5.png" width="80%" style="border:1px solid #999;margin-top:10px;" />  
    Dark blue names indicate the space names, and light blue signifies pages with children.  
1. Click a light blue name, like the __Welcome to the Confluence Demonstration Space__ page name.  
    You should see the menu snap open to display the page children:  
    <img src="../assets/images/confluence-gardener-1.png" width="80%" style="border:1px solid #999;margin-top:10px;" />  
    Blue pages have children, whereas grey pages have no child pages underneath. You can also use your mouse to zoom in and out – 
    just scroll your trackball up and down.   

As you explore your Gardener add-on, you might notice that you're not able to actually remove pages. Let's fix that in the next 
step. 

## <a name="dialog"></a> Display a full-screen dialog

When you attempt to remove a page, nothing happens. In this step, you'll add a [a full-screen 
dialog](../javascript/module-Dialog.html) to confirm the action,
and make sure it actually works. 

First, you'll declare the dialog in your `atlassian-connect.json` descriptor file. These dialogs are full-fledged AUI dialogs 
that exist in the parent frame (not in the same iframe Gardener uses). We'll provide the HTML source for the dialog, you'll 
register a new [`webItem`](../modules/common/web-item.html) that loads inside the
full-screen dialog.

1. Open `atlassian-connect.json` in your editor.  
1. Add the following snippet after the `generalPages` entry in the `modules` object:
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

    <a data-replace-text="Hide atlassian-connect.json [-]" class="aui-expander-trigger" aria-controls="complete-descriptor-with-webitem">Show atlassian-connect.json [+]</a> with the dialog `webItems` entry

    <div id="complete-descriptor-with-webitem" class="aui-expander-content">
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
1. Reinstall your add-on with the same Bash script as before:  
    <pre><code data-lang="text">$ ./install-confluence-gardener.sh</code></pre>  
    Alternatively, you can [reinstall your add-on in the UPM](#hosting-locally).
    Your add-on won't show any changes – yet. In the next step, you'll implement a function to display the dialog.
1. In your editor, open `removePageDialog.js`. 
    You should see another empty function: 
    <pre><code data-lang="javascript">
    define(function() {
        return function (deleteCallback) {
        }
    });
    </code></pre>
1. Load the [`dialog`](../javascript/module-Dialog.html)
    and [`events`](../javascript/module-Events.html) modules using `AP.require`:
    <pre><code data-lang="javascript">
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
    </code></pre>
1. After creating the dialog, unsubscribe from any previous `confirmPageRemoval` event bindings:
   <pre><code data-lang="javascript">events.offAll("confirmPageRemoval");</code></pre>
1. Now, try subscribing to `confirmPageRemoval` event bindings using `events.on` to register the event and
   passing `deleteCallback`.

    <a data-replace-text="Hide removePageDialog.js [-]" class="aui-expander-trigger" aria-controls="complete-remove-dialog">Show removePageDialog.js with subscription to `confirmPageRemoval` [+]</a>

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
1. In Confluence Gardener, click __Remove__ on a page. 
    You should see a dialog appear: 
    <img src="../assets/images/confluence-gardener-dialog.png" width="80%" style="border:1px solid #999;margin-top:10px;" />  
    If you don't see a dialog, try a hard refresh, or turning off the cache in your browser developer console. 

## What's next? 

You've built an add-on that can help you prune pages from Confluence spaces. Now you're ready to explore our [Connect Cookbook](./connect-cookbook.html).
