# Context parameters

Context parameters are additional values pairs that are sent to your add-on in the request URL from the host application.

## Table of contents

* [Standard parameters](#standard-parameters)
* [Additional parameters](#additional-parameters)
  * [JIRA](#additional-parameters-jira)
  * [Confluence](#additional-parameters-confluence)

## <a name="standard-parameters"></a>Standard parameters

The following parameters are common across all requests and are always included in the URL query string.

* `lic`: the [license status](./licensing.html#license-status)
* `loc`: the user's locale (eg: `en-GB`)
* `tz`: the user's timezone (eg: `Australia/Sydney`)
* `cp`: the context path of the instance (eg: `/wiki`)
* `xdm_e`: the base url of the host application, used for the Javascript bridge (xdm - cross domain messaging)
* `xdm_c`: the xdm channel to establish communication over
* `user_id`: the user's username. This may change and `user_key` should be used instead <span class="aui-lozenge">DEPRECATED</span>
* `user_key`: the user's unique identifier <span class="aui-lozenge">DEPRECATED</span>

**NOTE** The `user_id` and `user_key` parameters are deprecated in favour of the [`context.user` JWT claim](./understanding-jwt.html#token-structure-claims).

## <a name="additional-parameters"></a>Additional parameters

Your Atlassian Connect add-on can also request context parameters from the Atlassian application by using
variable tokens in the URL attributes of your modules. The add-on can use this to present content specific to the
context, for example, for the particular JIRA issue or project that is open in the browser. A particular variable is
available only where it makes sense given the application context.

URL variables are available to any of the page modules, including web panels, web items, general pages and dialog pages,
except for Confluence macros. To add a variable to a URL, enclose the variable name in curly brackets, as follows: `{variable.name}`.
The context variable must be either a path component or the `value` in a query string parameter formatted as `name=value`.

For example, the following URL includes variables that are bound to the JIRA project id and current issue key at runtime:

```
{
    "url": "/myPage/projects/{project.id}?issueKey={issue.key}"
}
```

Note that conventional URL encoding means that context parameters passed as a query parameter will be encoded
slightly differently from those included as a path component. The path component will use 
[percent encoding](https://en.wikipedia.org/wiki/Percent-encoding), while the query component will use 
[`application/x-www-form-urlencoded` encoding](http://www.w3.org/TR/html5/forms.html#application/x-www-form-urlencoded-encoding-algorithm). 
The primary difference to be aware of is that a space in a query parameter will be encoded as a `+`, while in the path 
component it will be encoded as `%20`.

If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.

## <a name="additional-parameters-jira"></a>JIRA

JIRA supports these context variables.

 * `issue.id`, `issue.key`, `issuetype.id`
 * `project.id`, `project.key`
 * `profileUser.name`, `profileUser.key` (available for user profile pages)
 * `dashboardItem.id`, `dashboardItem.key`, `dashboardItem.viewType`, `dashboard.id` (available for dashboard items)

JIRA issue pages only expose issue and project data. Similarly, version and component information is available only in
project administration pages.

#### <a name="additional-parameters-jira-software"></a>JIRA Software

JIRA Software supports these context variables.

 * `board.id`, `board.type`, 
 `board.screen` (available for plugin points that are displayed in multiple board screens),
 `board.mode` <span class="aui-lozenge">DEPRECATED</span> in favor of `board.screen` 
 * `sprint.id`, `sprint.state` 

## <a name="additional-parameters-confluence"></a>Confluence

Confluence supports these context variables.

 * `content.id`, `content.version`, `content.type`, `content.plugin`
 * `space.id`, `space.key`
 * `page.id`, `page.version`, `page.type` <span class="aui-lozenge">DEPRECATED</span>

Confluence provides the `content.*` variables wherever a page, blog post, or custom content is present. Some examples are
viewing or editing a page / blog post, or viewing a Confluence Question.

**NOTE** The `page.*` variables are available, but have been deprecated in favor of their `content.*` counterparts.

The `content.plugin` variable is a special case variable that is only present if the content in question is a Custom
Content entity provided by a plugin. For example, a question in Confluence Questions will have a `content.plugin` value
of "com.atlassian.confluence.plugins.confluence-questions:question". As a general rule, the `content.plugin` variable
will only be present if `content.type` is equal to "custom".
