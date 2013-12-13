# Migrating from the XML to JSON descriptors

## Resources
 * [Getting Started](getting-started.html) - the hello world tutorial has been renamed to the getting started guide and now uses the new JSON descriptor format for all examples.
 * [Modules](#modules) - You can view all available modules and their respective documentation under the "Jira Modules" and "Confluence Modules" sections of the navigation menu.

## Basic Add-on information

The information contained in these two descriptors is identical. It shows the new format for the basic information about your addon.
```
<?xml version="1.0" ?>
<atlassian-plugin key="myaddon_helloworld" name="Hello World" plugins-version="2">
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
    "description": "Atlassian Connect add-on",
    "key": "myaddon_helloworld",
    "baseUrl": "http://localhost:8000",
    "name": "Hello World",
    "authentication": {
        "type": "JWT"
    },
    "vendor": {
        "name": "My Organization, Inc",
        "url": "https://developer.atlassian.com"
    },
    "version": "1.0"
}
```

## Lifecycle Events
installation, enabled, disabled and uninstalled webhooks have been renamed to life cycle events. Below is an example:

```
    {
        "baseUrl": "http://www.example.com",
        "key": "my-add-on",
        "lifecycle": {
            "disabled": "/disabled",
            "enabled": "/enabled",
            "installed": "/installed",
            "uninstalled": "/uninstalled"
        }
    }
```

## Webhooks

Webhooks are available in the new JSON format by adding a `webhooks` json object to the root of the json descriptor.

```
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
```

To read more about webhooks: visit the [webhook module documentation](../modules/jira/webhooks.html)

## Conditions
The [Conditions documentation](conditions.html) as been updated to display the new format.

A simple condition can be converted as follows:
```
<conditions>
    <condition name="user_is_logged_in" />
</conditions>
```

```
"conditions": [
    {
        "condition": "user_is_logged_in",
        "invert": false
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
        "condition": "http://example.com/condition/onlyBettyCondition"
    }
]
```

## Modules
Not all modules have been directly mapped in the new descriptor. This means you will need to check the appropriate module configuration from this documentation and apply it to your own configuration.
The [Getting Started guide](getting-started.html) provides a good example of a `general-page` json descriptor.

### Dialog Page
The `dialog-page` module has been removed. You can achieve the same result by defining a webitem with a target of dialog.

```
{
    "modules": {
        "webItems": [{
            "location": "system.top.navigation.bar",
            "target": {
                "type": "dialog"
            },
            "url": "/dialog",
            "name": {
                "value": "Hello World"
            }
        }]
    }
}
```

## Atlassian Connect Express

[atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) now contains support for JWT and the `atlassian-plugin.json`. By upgrading these framework you will benifit from the new functionality.
### To upgrade

 * Change the version number of `atlassian-connect-express` in the `package.json` file to the latest version
 * npm update
 * update your atlassian-plugin.xml to atlassian-plugin.json


## Atlassian Connect Play
[atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java)

By upgrading to the new version of [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java) you will gain support for JWT and the new atlassian-plugin.json descriptor format.

## oAuth
<div class="aui-message warning shadowed information-macro">
    oAuth has been deprecated. Please consider migrating to [JWT](JWT.html).
</div>

If your add-on was created using the [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java) or [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) frameworks you can download the latest version for JSON descriptor and JWT support.

The `atlassian-plugin.json` now contains an `authentication` section that can be used to specify your oAuth credentials as follows:

```
{
    "name": "My sample Add-on",
    "authentication": {
        "type": "oAuth",
        "accessTokenUrl": "",
        "authorizeUrl": "",
        "callback": "",
        "publicKey": "S0m3Publ1cK3y",
        "requestTokenUrl": ""
    }
}
```


# Licensing
`atlassian-licensing-enabled` is now a top level boolean element.

```
{
    enableLicensing: true,
    name: "My sample Add-on"
}
```
