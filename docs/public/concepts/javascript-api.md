# Javascript API

## Sandboxing
An iframe instance whose parent and child reside on different domains or hostnames constitutes a
[sandboxed environment](http://en.wikipedia.org/wiki/Sandbox_%28computer_security%29).
The contained page has no access to its parent. These restrictions are imposed by
the browser's [same origin policy](http://en.wikipedia.org/wiki/Same_origin_policy).

There are a few limitations applicable to iframes:

 * Stylesheet properties from the parent do not cascade to the child page
 * Child pages have no access to its parent's DOM and JavaScript properties
 * Likewise, the parent has no access to its child's DOM or JavaScript properties

However, Atlassian Connect makes use of a technique called [cross-domain messaging](http://easyxdm.net/wp/).
This technique provides a string-based transport stack that allows communication between the iframe and its parent
using one of several available techniques. The most efficient technique is based on the browser's capabilities.

Atlassian Connect transparently enables cross-domain messaging in its page modules. One benefit you'll see from this
is that your add-on's page modules are automatically resized based on its content when loaded on the page.

Only content within an element with the class `ac-content` or the id `content` will be resized automatically. 
Without an element with either of those identifiers, the size of the body is used
without any automatic resizing. 
```
<div class="ac-content">
    <p>Hello World</p>
</div>
```

## Sharing data between iframes
A single add-on can generate multiple iframes in a particular page in the target application. Depending on the use case
for the add-on, the iframes may need to share information between each other.

The Atlassian Connect JavaScript client library, `all.js`, provides a [publish/subscribe mechanism](../javascript/module-Events.html)
that you can use to exchange data between iframes.

A common scenario in which a single add-on presents multiple iframes in a page is where a web panel or other page element
spawns a dialog box.

The only restriction on the data shared in this manner is that it must be serializable to JSON, the format in which the
data is conveyed on the shared bus.

For more information on the event API [visit the events documentation](../javascript/module-Events.html).

## JavaScript client library

Atlassian Connect provides a JavaScript client library called all.js. The Atlassian application hosts this file, making
it available at the following location relative to the Atlassian application URL:

```
http://{HOSTNAME}:{PORT}/{CONTEXT}/atlassian-connect/all.js
```

For example:

```
http://localhost:2990/jira/atlassian-connect/all.js
```

This library establishes the cross-domain messaging bridge with its parent. It also provides several methods and objects
that you can use in your pages without making a trip back to your add-on server.

You must add the `all.js` script to your pages in order to establish the cross-domain messaging bridge. Make sure your
pages include the following script:

```
<script src="https://{HOSTNAME}:{PORT}/{CONTEXT}/atlassian-connect/all.js"></script>
```

If you're using the [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) client
library to build your add-on, this will automatically be inserted into your pages at run time.

#### Note:
Don't download the all.js file and serve it up from your add-on server directly. The all.js file must be served up by
the product host in order for the cross-domain messaging bridge to be established.

### Options

The JavaScript client library has some configuration options that allow to customize its behavior. The options are passed
using the `data-options` attribute.

```
<script src="https://{HOSTNAME}:{PORT}/{CONTEXT}/atlassian-connect/all.js" data-options="option1:value;option2:value"></script>
```

The following options are currently supported:

<table class='aui'>
    <thead>
        <tr>
            <th>Option</th>
            <th>Values</th>
            <th>Default</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>`resize`</td>
            <td>`true` or `false`</td>
            <td>`true`</td>
            <td>You can deactivate the automatic resizing by setting `resize=false`.</td>
        </tr>
        <tr>
            <td>`sizeToParent`</td>
            <td>`true` or `false`</td>
            <td>`false`</td>
            <td>With `sizeToParent:true`, the iframe will take up its parent's space
            (instead of being sized to its internal content).</td>
        </tr>
        <tr>
            <td>`margin`</td>
            <td>`true` or `false`</td>
            <td>`true`</td>
            <td>If `true`, the `margin` option sets the body element's top, right and left margin to 10px for dialogs,
            and to 0 for non-dialog pages.</td>
        </tr>
        <tr>
            <td>`base`</td>
            <td>`true` or `false`</td>
            <td>`false`</td>
            <td>With `base:true`, a base tag pointing to the host page is injected: `<base href="{host}" target="_parent" />`.
            This can be useful for embedded links to product pages.</td>
        </tr>
    </tbody>
</table>

### Debugging `all.js`

A non-compressed version of the all.js javascript can be viewed by replacing `all.js` with `all-debug.js` for example:

```
<script src="https://{OnDemand hostname}/{context}/atlassian-connect/all.js"></script>
<!-- replace with -->
<script src="https://{OnDemand hostname}/{context}/atlassian-connect/all-debug.js"></script>
```

This can be helpful when trying to trace errors or debug the add-on javascript.

## Note on URL Encoding
URL query parameters are encoded as `application/x-www-form-urlencoded`.
This converts spaces to `+` which can cause issues when using JavaScript functions such as `decodeURIComponent`.
A simple way to handle this is to convert `+` to `%20` before decoding. A utility function `decodeQueryComponent`
is provided for this purpose. e.g

```
AP.require("_util", function(util){
  alert(util.decodeQueryComponent(window.location.href));
});
```
