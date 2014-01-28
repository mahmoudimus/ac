## Sandboxing
An iframe instance whose parent and child reside on different domains or hostnames constitutes a [sandboxed environment](http://en.wikipedia.org/wiki/Sandbox_%28computer_security%29). The contained page has no access to its parent. These restrictions are imposed by the browser's [same origin policy](http://en.wikipedia.org/wiki/Same_origin_policy).

There are a few limitations applicable to iframes:

 * Stylesheet properties from the parent do not cascade to the child page
 * Child pages have no access to its parent's DOM and JavaScript properties
 * Likewise, the parent has no access to its child's DOM or JavaScript properties

However, Atlassian Connect makes use of a technique called [cross-domain messaging](http://easyxdm.net/wp/). This technique provides a string-based transport stack that allows communication between the iframe and its parent using one of several available techniques. The most efficient technique is based on the browser's capabilities.

Atlassian Connect transparently enables cross-domain messaging in its page modules. One benefit you'll see from this is that your add-on's page modules are automatically resized based on its content when loaded on the page.

## Debugging `all.js`

A non-compressed version of the all.js javascript can be viewed by replacing `all.js` with `all-debug.js` for example:

```
<script src="https://{OnDemand hostname}/{context}/atlassian-connect/all.js"></script>
<!-- replace with -->
<script src="https://{OnDemand hostname}/{context}/atlassian-connect/all-debug.js"></script>
```

This can be helpful when trying to trace errors or debug the add-on javascript.

## Sharing data between iframes
A single add-on can generate multiple iframes in a particular page in the target application. Depending on the use case for the add-on, the iframes may need to share information between each other.

The Atlassian Connect JavaScript client library, `all.js`, provides a [publish/subscribe mechanism](../javascript/module-Events.html) that you can use to exchange data between iframes.

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    A common scenario in which a single add-on presents multiple iframes in a page is where a web panel or other page element spawns a dialog box.
</div>

The only restriction on the data shared in this manner is that it must be serializable to JSON, the format in which the data is conveyed on the shared bus.

For more information on the event API [visit the events documentation](../javascript/module-Events.html).

### JavaScript client library

Atlassian Connect provides a JavaScript client library called all.js. The Atlassian application hosts this file, making it available at the following location relative to the Atlassian application URL: 

    <hostname>/<app_context>/atlassian-connect/all.js

For example:

    http://atlas-laptop:2990/jira/atlassian-connect/all.js

This library establishes the cross-domain messaging bridge with its parent. It also provides several methods and objects that you can use in your pages without making a trip back to your add-on server.

You must add the `all.js` script to your pages in order to establish the cross-domain messaging bridge. Make sure your pages include the following script:

    <script src="https://{OnDemand hostname}/{context}/atlassian-connect/all.js"></script>

If you're using the [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) client library to build your add-on, this will automatically be inserted into your pages at run time.

<div class="aui-message warning">
    <p class="title">
        <span class="aui-icon icon-warning"></span>
        <strong>Important</strong>
    </p>
    Don't download the all.js file and serve it up from your add-on server directly. The all.js file must be served up by the parent in order for the cross-domain messaging bridge to be established.
</div>
