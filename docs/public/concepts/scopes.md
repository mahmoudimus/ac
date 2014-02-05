# Scopes

Scopes allow an add-on to request a particular level of access to an Atlassian product.

The following scopes are available for use by Atlassian Connect add-ons:

#### JIRA

* `READ` &ndash; can view, browse, read information from JIRA
* `WRITE` &ndash; can create or edit content in JIRA, but not delete them
* `DELETE` &ndash; can delete entities from JIRA
* `PROJECT_ADMIN` &ndash; can administer a project in JIRA
* `ADMIN` &ndash; can administer the entire JIRA instance

See the following pages for details on which remote endpoints are available in JIRA:

* [JIRA REST](../scopes/jira-rest-scopes.html)
* [JIRA JSON-RPC](../scopes/jira-jsonrpc-scopes.html)
* [JIRA SOAP](../scopes/jira-soap-scopes.html)

#### Confluence

* `READ` &ndash; can view, browse, read information from Confluence
* `WRITE` &ndash; can create or edit content in Confluence, but not delete them
* `DELETE` &ndash; can delete entities from Confluence
* `SPACE_ADMIN` &ndash; can administer a space in Confluence
* `ADMIN` &ndash; can administer the entire Confluence instance

See the following pages for details on which remote endpoints are available in Confluence:

* [Confluence REST](../scopes/confluence-rest-scopes.html)
* [Confluence JSON-RPC](../scopes/confluence-jsonrpc-scopes.html)
* [Confluence XML-RPC](../scopes/confluence-xmlrpc-scopes.html)
