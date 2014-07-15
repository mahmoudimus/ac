# Context Parameters

Context parameters are additional query string values that are sent to your add-on on each request. The following
parameters are common across all requests:

* `lic`: the [license status](./licensing.html#license-status)
* `loc`: the user's locale (eg: `en-GB`)
* `tz`: the user's timezone (eg: `Australia/Sydney`)
* `cp`: the context path of the instance (eg: `/wiki`)
* `xdm_e`: the base url of the host application, used for the Javascript bridge (xdm - cross domain messaging)
* `xdm_c`: the xdm channel to establish communication over

Additionally, your Atlassian Connect add-on can receive context parameters from the Atlassian application by using
variable tokens in the URL attributes of your modules. The add-on can use this to present content specific to the
context, for example, for the particular JIRA issue or project that is open in the browser.

JIRA supports these context variables:

 * `issue.id`, `issue.key`
 * `project.id`, `project.key`
 * `version.id`
 * `component.id`
 * `profileUser.name`, `profileUser.key` (this is available on the view user profile page)

Confluence supports these context variables:

 * `page.id`, `page.version`, `page.type`
 * `space.id`, `space.key`
 * `content.id`, `content.version`, `content.type`, `content.plugin`

A particular variable is available only where it makes sense given the application context. For example, a JIRA issue
page only exposes issue and project data. Similarly, version and component information is available only in project
administration pages.

In Confluence, the `content.*` variables are currently only available in certain contexts where custom content entities
reside, the primary example being Confluence Questions. It is intended that the `content` variables will be populated
and identical to their `page.*` counterparts in the next release however. `content.plugin` is a special case that is
populated only if the content in context is a custom Confluence content item, such as a Confluence Question. In this
case `content.type` will be set to "custom".

URL variables are available to any of the page modules, including web panels, web items, general pages and dialog pages,
except for Confluence macros. To add a variable to a URL, enclose the variable name in curly brackets, as follows: `{variable.name}`

For example, the following URL includes variables that are bound to the JIRA project id and current issue key at runtime:
```
{
    "url": "/myPage?issueKey={issue.key}&projectId={project.id}"
}
```
If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.
