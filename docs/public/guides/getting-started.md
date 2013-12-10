# Getting Started

## Add-on Descriptor
The add-on descriptor is an JSON file (`atlassian-plugin.json`) that describes the add-on to the Atlassian application. If you're familiar with Java add-on development with previous versions of the Atlassian Framework, you should already be familiar with plugin descriptors.

The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for an Atlassian OnDemand instance subscribes to an add-on, the Atlassian instance retrieves the add-on descriptor from its published location. 

The descriptor includes general information for the add-on (in the `modules` element). It also declares the modules that the add-on wants to use or extend, as described in the following sections.

### About `modules`
The `modules` element tells the Atlassian OnDemand instance about the add-on. Among other things, the descriptor informs the instance about the name and description of the add-on and the modules it wants to implement.

Let's look at an example:

```
{
    "description": "hello world description",
    "key": "com.example.tutorial.myaddon",
    "name": "My Addon",
    "vendor": {
        "name": "Example, Inc.",
        "url": "https://www.example.com"
    },
    "version": "1.0"
}
```
Omitted here are any module descriptors. We'll cover those in the next section.

Also, for details and application-specific reference information on the descriptor, you should always refer to the [Interactive Descriptor Guide](https://developer.atlassian.com/display/AC/Interactive+Descriptor+Guide). But we'll call out a few highlights from the example here.

For the `description` value, you should supply a brief textual description of your add-on. When your add-on is installed in the Atlassian application, this information appears with the add-on in the Manage Add-ons page of the administration console. Thus, your description should provide meaningful and identifying information for the instance administrator. 

The version element identifies the version of the add-on itself. Note that versioning in general works a little differently in Atlassian Connect add-ons than it does in traditional, in-process add-ons.

Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be enhanced or patched at any time, without having to report the change to the Atlassian instance. The changes are reflected in the Atlassian instance immediately (or at least at page reload time).

However, some add-ons changes do require a change in the descriptor file as well. For example, say you modify the add-on to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated descriptor available, which instances will have to re-register. To implement this change, you need to create a new version of the add-on in its Marketplace listing. The Marketplace and UPM will take care of the rest: informing administrators of the available update.

For more information about the permissions and license-related elements, see [Scopes](https://developer.atlassian.com/display/AC/Scopes) and [Licensing](https://developer.atlassian.com/display/AC/Licensing).


### About Modules
A module is a service or extension point in the Atlassian application that add-ons use to integrate with the Atlassian application. 

An add-on can implement as many modules as needed. For example, a typical add-on would likely provide modules for at least one webhook, a configuration page, and possibly multiple general pages.

All modules declarations must have a `url` attribute. The url attribute identifies the path on the add-on host to the resource that implements the module. The URL value should be valid relative to the display-url value of the remote-plugin-container value in the add-on descriptor. 

<div class="aui-message warning shadowed information-macro">
    You can add variables to URL attributes to receive context information from the Atlassian application, such as the current JIRA issue or Confluence page ID. For more information, see the following section.
</div>

For a webhook, the URL should be the address to which the Atlassian instance posts notifications. For page modules, such as `generalPages`, identifies the web content to be used to compose the page.

Here's an example of a module declaration:
```
{
    "modules": {
        "webItems": [{
            "conditions": [
                {
                    "condition": "sub_tasks_enabled"
                },
                {
                    "condition": "is_issue_editable"
                },
                {
                    "condition": "is_issue_unresolved"
                }
            ],
            section: "operations-subtasks",
            target: "dialog",
            "url": "/dialog",
            "name": {
                "value": "Create Sub-Tasks"
            }
        }]
    }
}
```
In this case, we're declaring a `dialog-page` webItem. This declaration adds a dialog box to JIRA that users can open by clicking a "Create Sub-Tasks" link on an issue. The intent of the add-on is to provide an easy way to create subtasks for the current issue. 

You can specify the conditions in which the link (and therefore access to this page) appears. The Atlassian application ensures that the link only appears if it is appropriate for it to do so. In the example, the module should only appear if subtasks are enabled. The condition elements state conditions that must be true for the module to be in effect.

The url value in our example is /dialog. This must be a resource that is accessible on the server (relative to the base URL of the add-on). It presents the content that appears in the iframe dialog, in other words, the HTML, JavaScript, or other type of web content source that composes the iframe content. 

### Receiving context parameters ###
An Atlassian Connect add-on module can receive context parameters from the Atlassian application by using variable tokens in the URL attribute value. The add-on can use this to present content specific to the context, for example, for the particular JIRA issue or project that's open in the browser.

JIRA supports these context variables:

 * issue.id, issue.key
 * project.id, project.key
 * version.id
 * component.id
 * profileUser.name, profileUser.key (this is available on the View User Profile page; for example: /secure/ViewProfile.jspa?name=jcreenaune)

Confluence supports these context variables:

 * page.id
 * space.id, space.key

A particular variable is available only where it makes sense given the application context. For example, a JIRA issue page only exposes issue and project data. Similarly, version and component information is available only in project administration pages.

URL variables are available to any of the page modules, including web panels, web items, general pages and dialog pages, except for Confluence macros. To add a variable to a URL, enclose the variable name in curly brackets, as follows: `${variable.name}`

For example, the following URL includes variables that are bound to the JIRA project id and current issue key at runtime:
```
url="/myPage?issueKey=${issue.key}&amp;projectId=${project.id}"
```
If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.

## Installing your Add-on
You can install add-ons using the add-on manager for Atlassian applications, the [Universal Plugin Manager (UPM)](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation). With the UPM, you can either register the add-on through the UI, similar to how an administrator would, or using UPM's REST API. After registration, the add-on appears in the list of user-installed add-ons in the [Manage Add-ons](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation) page in the administration console and its features are available for use in the target application. 

### Installing an add-on using the Universal Plugin Manager

<div class="diagram">
participant Admin
participant Add_on_server
participant OnDemand
Admin->OnDemand:Install add-on descriptor through UPM
OnDemand->Add_on_server:info about OnDemand instance\nincluding shared keys
OnDemand->Admin:Notified of successful installation 
</div>

Installing your add-on adds it to your OnDemand application. To be more precise, installing is really just registering the add-on with the application and the only thing that is stored by the application at this time is the add-on descriptor. 

You can install an add-on with the UPM as follows. Note, these instructions were written for UPM version 2.14 or later.

 1. Log in to the Atlassian application interface as an admin or a system administrator. If you started the application with Atlassian's SDK, the  default username/password combination is admin/admin.
 2. Choose <img src="../assets/images/cog.png" alt="Settings" /> > __Add-ons__ from the menu. The Administration page will display.
 3. Choose the __Manage add-ons__ option.
 4. Scroll to the page's bottom and click the __Settings__ link. The __Settings__ dialog will display. 
 5. Make sure the "Private listings" option is checked and click __Apply__.
 6. Scroll to the top of the page and click the __Upload Add-on__ link.
 7. Enter the URL to the hosted location of your plugin descriptor. In this example, the URL is similar to the following:  http://localhost:8000/atlassian-plugin.xml. (If you are installing to an OnDemand instance, the URL must be served from the Marketplace, and will look like https://marketplace.atlassian.com/download/plugins/com.example.add-on/version/39/descriptor?access-token=9ad5037b)
 8. Press __Upload__. The system takes a moment to upload and register your plugin. It displays the __Installed and ready to go__ dialog when installation is complete. <img width="100%" src="../assets/images/installsuccess.jpeg" />
 9. Click __Close__.
 10. Verify that your plugin appears in the list of __User installed add-ons__. For example, if you used Hello World for your plugin name, that will appears in the list.


### Installing an add-on using the REST API
You can also install an add-on using the UPM's REST API. You'll find this method useful if you want to install add-ons programmatically, say from a script, or simply want to quickly install an add-on from the command line. Broadly speaking, installing an add-on (or performing any operation against the REST API of the UPM) is a two-step process:

First get a UPM token.

Next, issue the request to the REST API, including the token you received.

The following steps walk you through these steps in detail:
Send a GET request to the following resource: 
`http://HOST_NAME:PORT/CONTEXT/rest/plugins/1.0/?os_authType=basic`
In your request:

 1. Replace `HOST_NAME` and `PORT` with the actual host name and port of the target Atlassian application. If applicable (i.e., if using a development instance), include the `CONTEXT` with the application context (/jira or /confluence).
 2. Include the username and password for a system administrator user in the target Atlassian application as HTTP Basic Authentication credentials.
 3. Set the Accept header for the request to: "`application/vnd.atl.plugins.installed+json`"

Capture the header named "upm-token" in the response. This is the UPM token you need for the next request.
Now install your add-on by issuing a POST request to the following resource:
`http://HOST_NAME:PORT/CONTEXT/rest/plugins/1.0/?token=${upm-token}`
In your request:

 1. Again use the actual host name and port and path for your target Atlassian application.
 2. The token value should be the value of the upm-token header you just received.
 3. In the request, set the Accept header to: "`application/json`"
 4. Set the Content-Type for the data to: "`application/vnd.atl.plugins.install.uri+json`"
 5. In the body of the POST, include the following JSON data:

This registers the add-on declared by the `atlassian-plugin.json` file at the URL.

Note that you should not rely on the response returned from the POST request to confirm that the plugin has been installed. Instead, the best way to confirm that the plugin has been installed is to add a webhook to your add-on descriptor that listens for the add-on installation event. The webhook declaration in the `atlassian-plugin.json` file would look something like this:
```
<webhook key="installed" event="remote_plugin_installed" url="/your-url-here" />
```

### Troubleshooting authentication
When registering from the command line using [cURL](http://curl.haxx.se/docs/manpage.html), keep in mind that cURL does not perform session maintenance across calls (unlike other clients, such as Apache HttpClient). Thus, you need to either:

Send the authentication credentials in both requests, or

Have cURL save any cookies from the first request and send them in the second. That is:

 1. To save cookies, use the -c switch: `curl -c cookiesfile.txt` ...
 2. And then include the cookies in the second request: `curl -b cookiesfile.txt` ...
