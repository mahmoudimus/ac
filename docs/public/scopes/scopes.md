# Scopes

Scopes allow an add-on to request a particular level of access to an Atlassian product. Each add-on
must specify what scopes it requests. The user will be informed of these scopes at installation
time. If you find that your add-on needs to request additional scopes, each user will have to
manually confirm this "scope expansion" at the time of upgrade. Note: the old version of your
descriptor will continue to be in effect until the scope expansion is accepted by the user.

The following scopes are available for use by Atlassian Connect add-ons:

#### JIRA

* `READ` &ndash; can view, browse, read information from JIRA
* `WRITE` &ndash; can create or edit content in JIRA, but not delete them
* `DELETE` &ndash; can delete entities from JIRA
* `PROJECT_ADMIN` &ndash; can administer a project in JIRA
* `ADMIN` &ndash; can administer the entire JIRA instance

#### Confluence

* `READ` &ndash; can view, browse, read information from Confluence
* `WRITE` &ndash; can create or edit content in Confluence, but not delete them
* `DELETE` &ndash; can delete entities from Confluence
* `SPACE_ADMIN` &ndash; can administer a space in Confluence
* `ADMIN` &ndash; can administer the entire Confluence instance

You can see what API methods are available in each scope for each API below:

* [JIRA REST APIs](./jira-rest-scopes.html)
* [JIRA JSON-RPC APIs](./jira-jsonrpc-scopes.html)
* [JIRA SOAP APIs](./jira-soap-scopes.html)
* [Confluence REST APIs](./confluence-rest-scopes.html)
* [Confluence JSON-RPC APIs](./confluence-jsonrpc-scopes.html)
* [Confluence XML-RPC APIs](./confluence-xmlrpc-scopes.html)