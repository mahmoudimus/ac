# Scopes

Scopes allow an add-on to request a particular level of access to an Atlassian product.

For example:

* Within a particular product instance an administrator may further limit the actions that an add-on may perform. This
is valuable because it allows administrators to safely install add-ons that they otherwise would not.
* The scopes may allow the *potential* to access beta or non-public APIs that are later changed in or removed from the
Atlassian product. The inclusion of the API endpoint in a scope does not imply that the product makes this endpoint
public: read the product's API documentation for API details.

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

### Example

Scopes are declared as a top level attribute of the [`atlassian-connect.json` descriptor](../modules/):

    {
        "baseUrl": "http://my-addon.com",
        "key": "atlassian-connect-addon"
        "modules": {},
        "scopes": [
            "read", "write"
        ]
    }
