# Migrate to JSON module descriptors and JWT

## Resources
 * [Getting Started](./getting-started.html) - the hello world tutorial has been renamed to the getting started guide and now uses the new JSON descriptor format for all examples.
 * [Modules](../concepts/modules) - You can view all available modules and their respective documentation under the "JIRA Modules" and "Confluence Modules" sections of the navigation menu.

## Basic Add-on information

The information contained in these two descriptors is identical. It shows the new format for the basic information about your addon.
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
    "version": "1.0"
}
```

## Lifecycle Events
installed, enabled, disabled and uninstalled webhooks have been replaced with life cycle events. Below is an example:

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

The installed event is now synchronous, this means that if your `installed` lifecycle event URL does not return a 200 or
 204 response header, the add-on will fail to install. All other lifecycle events are asynchronous.

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

### Dialog Page

The `dialog-page` module has been removed. A replacement is coming in the near future.

### Confluence Macros

* The `<remote-macro>` XML element is replaced by the [`"staticContentMacros"`](../modules/confluence/static-content-macro.html) module
* The `<macro-page>` XML element is replaced by the [`"dynamicContentMacros"`](../modules/confluence/dynamic-content-macro.html) module
* The `<context-parameters>` XML element no longer exists. You can now use variable substitution to include macro parameters in the URL.


# OAuth
<div class="aui-message warning">
    <span class="aui-icon icon-warning"></span>
    OAuth has been deprecated. Please consider migrating to [JWT](../concepts/authentication.html).
</div>

If your add-on was created using the [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
or [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) frameworks you can download
the latest version for JSON descriptor and [JWT support](authentication.html).

The `atlassian-plugin.json` now contains an `authentication` section that can be used to specify your oAuth credentials
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
the `atlassian-plugin.json`. By upgrading these framework you will benefit from the new functionality.

<div class="aui-message warning">
    <span class="aui-icon icon-warning"></span>
    OAuth has been entirely removed from the latest version of ACE.
</div>

See: [upgrading ACE](./upgrade-ace.html)


## Atlassian Connect Play
[atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)

By upgrading to the new version of [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)
you will gain support for JWT and the new `atlassian-plugin.json` descriptor format.



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
