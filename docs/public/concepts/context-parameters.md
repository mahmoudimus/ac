### Context Parameters

Your Atlassian Connect add-on  can receive context parameters from the Atlassian application by using variable tokens
in the URL attributes of your modules. The add-on can use this to present content specific to the context, for example,
for the particular JIRA issue or project that's open in the browser.

JIRA supports these context variables:

 * issue.id, issue.key
 * project.id, project.key
 * version.id
 * component.id
 * profileUser.name, profileUser.key (this is available on the View User Profile page; for example: /secure/ViewProfile.jspa?name=jdoe)

Confluence supports these context variables:

 * page.id
 * space.id, space.key

A particular variable is available only where it makes sense given the application context. For example, a JIRA issue
page only exposes issue and project data. Similarly, version and component information is available only in project
administration pages.

URL variables are available to any of the page modules, including web panels, web items, general pages and dialog pages,
except for Confluence macros. To add a variable to a URL, enclose the variable name in curly brackets, as follows: `{variable.name}`

For example, the following URL includes variables that are bound to the JIRA project id and current issue key at runtime:
```
url="/myPage?issueKey={issue.key}&projectId={project.id}"
```
If the application isn't able to bind a value to the variable at runtime for any reason, it passes an empty value instead.
