# Conditions

A condition specifies requirements that must be met for a user to access the features or UI exposed by a module. For
instance, the condition can require a user to be an administrator, have edit permissions, and apply other requirements
for access. If the condition is not met, the panel, page, or other UI element exposed by the add-on does not appear on
the page.

Various types of modules accept conditions, including `generalPages`, `adminPages`, and `webItems`. To see whether a certain
module accepts conditions, see their specific module documentation page.

For a list of the available conditions for each product, [see the documentation for the Single Condition](../modules/fragment/single-condition.html).

## Remote Conditions

    {
        "name": "My Addon",
        "modules": {
            "generalPages": [
                {
                    "conditions": [
                        {
                            "condition": "/condition/onlyBettyCondition"
                        }
                    ]
                }
            ]
        }
    }

For a remote condition, the Atlassian application issues a request to the remote resource and expects a response which
specifies whether to show or hide the module feature.

    {
        "shouldDisplay": false
    }

The add-on can pass parameters to the remote condition as URL query parameters. Remote condition has request
authentication information passed through as a header, rather than as a query string parameter.

Remote conditions are URLs and must start with either 'http' or '/'.


## Static conditions

A static condition is a condition which is exposed from the host Atlassian application.

For example, a condition that will evaluate when only anonymous users view the page is specified by the following
module declaration:

```
{
    "name": "My Addon",
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "user_is_logged_in",
                        "invert": true
                    }
                ]
            }
        ]
    }
}
```

### Condition parameters

Certain static conditions also accept parameters. For example:

* `has_issue_permission`
* `has_project_permission`

These conditions restrict access to the modules based upon user permission settings for the issue or project.
Note that behind the scenes, the issue permission check simply checks the project context for the issue and conducts the
permission check for the user against that project.

You can pass parameters to conditions as follows:

```
{
    "name": "My Addon",
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "has_issue_permission",
                        "invert": false,
                        "params": {
                            "permission": "resolv"
                        }
                    }
                ]
            }
        ]
    }
}
```

In this case, the user must have not just access to the issue but resolve permissions specifically. The permissions applicable
to Atlassian Connect JIRA add-on modules are equivalent to those applicable to JIRA Java plugin development, as described
in the [JIRA Permissions class reference](https://docs.atlassian.com/jira/latest/com/atlassian/jira/security/Permissions.html)
documentation.


# Product Specific Conditions
For a list of the available conditions for each product, [see the documentation for the Single Condition](../modules/fragment/single-condition.html).

