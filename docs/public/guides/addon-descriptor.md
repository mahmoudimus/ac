# Add-on Descriptor
The add-on descriptor is an JSON file (`atlassian-connect.json`) that describes the add-on to the Atlassian application.
If you're familiar with Java add-on development with previous versions of the Atlassian Framework, you should already be familiar with plugin descriptors.

The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for an Atlassian OnDemand instance subscribes to an add-on, the Atlassian instance retrieves the add-on descriptor from its published location. 

The descriptor includes general information for the add-on (in the `modules` element). It also declares the modules that the add-on wants to use or extend, as described in the following sections.

The `modules` element tells the application instance about the add-on. Among other things, the descriptor informs the instance about the name and description of the add-on and the modules it wants to implement.

Let's look at an example:

```
{
    "name": "My Addon",
    "description": "hello world description",
    "key": "com.example.tutorial.myaddon",
    "vendor": {
        "name": "Example, Inc.",
        "url": "https://www.example.com"
    },
    "baseUrl": "http://localhost:8000",
    "version": "1.0"
}
```
Omitted here are any module descriptors. We'll cover those in the next section.

Also, for details and application-specific reference information on the descriptor please refer to the "jira modules" and "confluence modules" sections of this documentation. But we'll call out a few highlights from the example here.

For the `description` value, you should supply a brief textual description of your add-on. When your add-on is installed in the Atlassian application, this information appears with the add-on in the Manage Add-ons page of the administration console. Thus, your description should provide meaningful and identifying information for the instance administrator. 

The version element identifies the version of the add-on itself. Note that versioning in general works a little differently in Atlassian Connect add-ons than it does in traditional, in-process add-ons.

Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be enhanced or patched at any time, without having to report the change to the Atlassian instance. The changes are reflected in the Atlassian instance immediately (or at least at page reload time).

However, some add-ons changes do require a change in the descriptor file as well. For example, say you modify the add-on to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated descriptor available, which instances will have to re-register. To implement this change, you need to create a new version of the add-on in its Marketplace listing. The Marketplace and UPM will take care of the rest: informing administrators of the available update.

For more information about the permissions and license-related elements, see [Scopes](scopes.html) and [Licensing](https://developer.atlassian.com/display/AC/Licensing).


## About Modules
A module is a service or extension point in the Atlassian application that add-ons use to integrate with the Atlassian application. 

An add-on can implement as many modules as needed. For example, a typical add-on would likely provide modules for at least one lifecycle element, a configuration page, and possibly multiple general pages.

All modules declarations must have a `url` attribute. The url attribute identifies the path on the add-on host to the resource that implements the module. The URL value must be valid relative to the `baseUrl` value in the add-on descriptor. 

<div class="aui-message warning shadowed information-macro">
    You can add variables to URL attributes to receive context information from the Atlassian application, such as the current JIRA issue or Confluence page ID. For more information, see the following section.
</div>

For a webhook, the URL should be the address to which the Atlassian instance posts notifications. For page modules, such as `generalPages`, identifies the web content to be used to compose the page.

Here's an example of a module declaration:
```
{
    "name": "My Addon",
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
            "location": "operations-subtasks",
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

## Receiving context parameters
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
url="/myPage?issueKey=${issue.key}&projectId=${project.id}"
```
If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.
