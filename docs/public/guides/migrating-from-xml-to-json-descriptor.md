# Migrate to JSON module descriptors and JWT

This document demonstrates how to convert a legacy `atlassian-plugin.xml` descriptor into one matching the new JSON
format. By convention, JSON descriptors are named `atlassian-connect.json`.

## Resources
 * [Getting Started](./getting-started.html) - the hello world tutorial has been renamed to the getting started guide and now uses the new JSON descriptor format for all examples.
 * [Modules](../modules) - You can view all available modules and their respective documentation under the "JIRA Modules" and "Confluence Modules" sections of the navigation menu.

## Basic Add-on information

The information contained in the two descriptors below is identical. It shows the new format for the basic information about your addon.

### atlassian-plugin.xml
```
<?xml version="1.0" ?>
<atlassian-plugin key="hello-world" name="Hello World" plugins-version="2">
    <plugin-info>
        <description>Atlassian Connect add-on</description>
        <version>1</version>
        <vendor name="My Organization, Inc" url="https://developer.atlassian.com" />
    </plugin-info>
    <remote-plugin-container key="container" display-url="http://localhost:8000">
    </remote-plugin-container>
</atlassian-plugin>
```

### atlassian-connect.json
```
{
    "name": "Hello World",
    "description": "Atlassian Connect add-on",
    "key": "hello-world",
    "baseUrl": "http://localhost:8000",
    "vendor": {
        "name": "My Organization, Inc",
        "url": "https://developer.atlassian.com"
    },
    "apiVersion": "1.0"
}
```

## Lifecycle Events
The `installed`, `enabled`, `disabled` and `uninstalled` webhooks have been replaced with [Lifecycle](../modules/lifecycle.html)
events. Below is an example:

```
{
    "name": "Hello World",
    "description": "Atlassian Connect add-on",
    "baseUrl": "http://www.example.com",
    "key": "myaddon_helloworld",
    "lifecycle": {
        "disabled": "/disabled",
        "enabled": "/enabled",
        "installed": "/installed",
        "uninstalled": "/uninstalled"
    }
}
```

If you declare an `installed` lifecycle event URL and it does not return a 200 or 204 response code, the Atlassian
product will consider the installation as failed and notify the user attempting to install the add-on. Response codes
from requests to other lifecycle URLs are ignored.

The `installed` payload also contains the details for handling JWT authenticated requests to and from the Atlassian
product. See [JWT Installation Handshake](../concepts/authentication.html#installation) for further details.

## Webhooks

Webhooks are available in the new JSON format by adding a `webhooks` json object to the modules element of the json descriptor.

```
{
    "name": "Hello World",
    "description": "Atlassian Connect add-on",
    "baseUrl": "http://www.example.com",
    "key": "myaddon_helloworld",
    "modules": {
        "webhooks": [
            {
                "event": "jira:issue_created",
                "url": "/issue-created"
            },
            {
                "event": "jira:issue_updated",
                "url": "/issue-updated"
            }
        ]
    }
}

```

To read more about webhooks:

 * [JIRA webhook module documentation](../modules/jira/webhooks.html)
 * [Confluence webhook module documentation](../modules/confluence/webhooks.html)

## Conditions

A simple condition can be converted as follows:
```
<conditions>
    <condition name="user_is_logged_in" />
</conditions>
```

```
"conditions": [
    {
        "condition": "user_is_logged_in"
    }
]
```

Remote conditions can be migrated as follows:
```
<conditions>
    <condition url="/onlyBettyCondition" />
</conditions>
```

```
"conditions": [
    {
        "condition": "/onlyBettyCondition"
    }
]
```

See the [Conditions documentation](../concepts/conditions.html) for more details.


## Modules
Not all modules have been directly mapped in the new descriptor. This means you will need to check the appropriate
module configuration from this documentation and apply it to your own configuration. The
[Getting Started guide](./getting-started.html) provides a good example of a `general-page` json descriptor.

See the [Confluence Module List](../modules/confluence/index.html) and [JIRA Module List](../modules/jira/index.html)
for a full list of all supported modules.

### Dialog Page

* The `dialog-page` module has been removed. It is replaced by the [`webItem`](../modules/jira/web-item.html) module with
the [`target`](../modules/fragment/web-item-target.html) attribute set to `dialog`.

### Confluence Macros

* The `<remote-macro>` XML element is replaced by the [`staticContentMacros`](../modules/confluence/static-content-macro.html) module.
* The `<macro-page>` XML element is replaced by the [`dynamicContentMacros`](../modules/confluence/dynamic-content-macro.html) module.
* The `<context-parameters>` XML element no longer exists. You can now use [variable substitution](../concepts/context-parameters.html) to include macro parameters in the URL.

### Project Config Tabs and Panels

* The `<project-config-tab>` XML element has been replaced by the [`jiraProjectAdminTabPanels`](../modules/jira/project-admin-tab-panel.html) module.
* The `<project-config-panel>` XML element no longer exists. Instead, use a [`webPanels`](../modules/jira/web-panel.html)
module with the `location` attribute set to `webpanels.admin.summary.left-panels` or `webpanels.admin.summary.right-panels`.

### Issue Panel Page

* The `<issue-panel-page>` XML element no longer exists. Instead, use a [`webPanels`](../modules/jira/web-panel.html)
module with the `location` attribute set to `atl.jira.view.issue.right.context`.

### Documentation Links

* The `<param name="documentation.url">` XML element has been replaced by adding a [`links`](../modules/#links) module
at the descriptor root.

# OAuth
<div class="aui-message warning">
    <span class="aui-icon icon-warning"></span>
    OAuth has been deprecated. Please consider migrating to [JWT](../concepts/authentication.html).
</div>

If your add-on was created using the [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
or [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) frameworks you can download
the latest version for JSON descriptor and [JWT support](../concepts/authentication.html).

The `atlassian-connect.json` now contains an `authentication` section that can be used to specify your OAuth credentials
as follows:

```
{
    "name": "My sample Add-on",
    "authentication": {
        "type": "oauth",
        "publicKey": "S0m3Publ1cK3y"
    }
}
```

## Atlassian Connect Express

[atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) now contains support for JWT and
the `atlassian-connect.json`. By upgrading these framework you will benefit from the new functionality.

<div class="aui-message warning">
    <span class="aui-icon icon-warning"></span>
    OAuth has been entirely removed from the latest version of ACE.
</div>

See: [upgrading ACE](./upgrade-ace.html)


## Atlassian Connect Play
[atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)

By upgrading to the new version of [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
you will gain support for JWT and the new `atlassian-connect.json` descriptor format.



# Licensing
`atlassian-licensing-enabled` is now a top level boolean element.

```
{
    "name": "Hello World",
    "key": "hello-world",
    "description": "Atlassian Connect add-on",
    "baseUrl": "http://www.example.com",
    "enableLicensing": true
}
```
