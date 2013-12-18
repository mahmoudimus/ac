# Modules

Modules are UI extension points that add-ons can use to insert content into various areas of the host application's interface. You implement a page module (along with others type of module you can use with Atlassian Connect, like webhooks) by declaring it in the [add-on descriptor](addon-descriptor.html) and implementing the add-on code that composes it.

Each application has module types that are specific for it, but there are some common types as well. For instance, both JIRA and Confluence support the `generalPages` module, but only Confluence has `profilePage`.

An add-on can implement as many modules as needed. For example, a typical add-on would likely provide modules for at least one lifecycle element, a configuration page, and possibly multiple general pages.

All modules declarations must have a `url` attribute. The url attribute identifies the path on the add-on host to the resource that implements the module. The URL value must be valid relative to the `baseUrl` value in the add-on descriptor. 

 * [JIRA modules](../modules/jira)
 * [Confluence modules](../modules/confluence)

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
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
