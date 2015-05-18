# Context Parameters

Context parameters are additional query string values that are sent to your add-on on each request. The following
parameters are common across all requests:

* `lic`: the [license status](./licensing.html#license-status)
* `loc`: the user's locale (eg: `en-GB`)
* `tz`: the user's timezone (eg: `Australia/Sydney`)
* `cp`: the context path of the instance (eg: `/wiki`)
* `xdm_e`: the base url of the host application, used for the Javascript bridge (xdm - cross domain messaging)
* `xdm_c`: the xdm channel to establish communication over
* `user_id`: the user's username. This may change and `user_key` should be used instead <span class="aui-lozenge">DEPRECATED</span>
* `user_key`: the user's unique identifier <span class="aui-lozenge">DEPRECATED</span>

The `user_id` and `user_key` parameters are deprecated in favour of the [`context.user` JWT claim](./understanding-jwt.html#token-structure-claims).

Additionally, your Atlassian Connect add-on can receive context parameters from the Atlassian application by using
variable tokens in the URL attributes of your modules. The add-on can use this to present content specific to the
context, for example, for the particular JIRA issue or project that is open in the browser.

JIRA supports these context variables:

 * `issue.id`, `issue.key`
 * `project.id`, `project.key`
 * `version.id`
 * `component.id`
 * `profileUser.name`, `profileUser.key` (this is available on the view user profile page)
 * `dashboardItem.id`, `dashboardItem.key`, `dashboard.id`, `dashboardItemViewType` (this are available for dashboard items)

Confluence supports these context variables:

 * `content.id`, `content.version`, `content.type`, `content.plugin`
 * `space.id`, `space.key`
 * `page.id`, `page.version`, `page.type` <span class="aui-lozenge">DEPRECATED</span>
 * `agileBoard.id`, `agileBoard.mode`

A particular variable is available only where it makes sense given the application context. For example, a JIRA issue
page only exposes issue and project data. Similarly, version and component information is available only in project
administration pages.

Confluence provides the `content.*` variables wherever a page, blogpost, or custom content is present. Some examples are
viewing or editing a page/blogpost, or viewing a Confluence Question. The `page.*` variables are available, but have
been deprecated in favour of their `content.*` counterparts.

The `content.plugin` variable is a special case variable that is only present if the content in question is a Custom
Content entity provided by a plugin. For example, a question in Confluence Questions will have a `content.plugin` value
of "com.atlassian.confluence.plugins.confluence-questions:question". As a general rule, the `content.plugin` variable
will only be present if `content.type` is equal to "custom".

URL variables are available to any of the page modules, including web panels, web items, general pages and dialog pages,
except for Confluence macros. To add a variable to a URL, enclose the variable name in curly brackets, as follows: `{variable.name}`.
The context variable must be either a path component or the `value` in a query string parameter formatted as `name=value`.

For example, the following URL includes variables that are bound to the JIRA project id and current issue key at runtime:
```
{
    "url": "/myPage/projects/{project.id}?issueKey={issue.key}"
}
```
If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.
