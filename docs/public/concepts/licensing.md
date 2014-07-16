# Licensing

Any add-on intended for sale on the [Atlassian Marketplace](https://marketplace.atlassian.com) needs
to be license-enabled. Add-ons work with the atlassian application to apply licensing requirements.

The Atlassian application reports the licensing status of the instance to the plugin in each request.

The add-on should:

 * check the license status in service requests it receives from the Atlassian application
 * implement the logic appropriate for the given license status. For instance, for an expired
   license, the add-on may choose to disable its features, enter a read-only state, or function
   in some other restricted manner that makes sense

## Development considerations

In a nutshell, to implement licensing for Atlassian Connect, you need to:

 * set the `enableLicensing` flag in the add-on descriptor for the add-on
 * write code to check the `lic` URL parameter for all incoming requests
 * use a REST resource at `/rest/atlassian-connect/1/license` to get additional license information
   for each application
 * implement the logic appropriate for the add-on based on the license status

#### Note:
The result of a license check by the Atlassian application is cached for 5 minutes.

## Setting the License-Enabled Flag

To implement licensing in an add-on, set the licensing enabled flag in the plugin descriptor to
true. Any add-on intended for sale should have this flag enabled.

The licensing enabled flag appears in the descriptor file `atlassian-connect.json`

```
{
    "name": "Hello World",
    "key": "hello-world",
    "description": "Atlassian Connect add-on",
    "baseUrl": "http://www.example.com",
    "enableLicensing": true
}
```

This tells UPM to check and report licensing status to the plugin. On the other hand, if you are
using a plugin strictly for internal use or you plan to distribute it freely on the Marketplace,
this should be set to false.

<a name="license-status"></a>
## Handling requests with the license status

Each incoming request from the Atlassian application instance includes a query parameter named lic.
For example:

```
http://....?lic=active
```

Your add-on should check this value to determine the license status of the instance associated with
the current request. The `lic` parameter may have one of three values:

 * `active` – the license is valid for this instance and add-on
 * `expired` – a license is present, but it is expired
 * `none` – no license is present

## Accessing License Details

In addition to the `lic` parameter, an add-on can use a REST API resource to get an instance's
license for this plugin. Note, you will need to declare `READ` [scope](../scopes/scopes.html) in
order to use this resource.

The license resource is exposed as a REST resource at this URL:

```
https://{HOSTNAME}:{PORT}/{CONTEXT}/rest/atlassian-connect/latest/license
```

Among other information, the plugin can use the resource to discover:

 * whether the plugin license for an instance is valid
 * the organisation name, SEN and contact email associated with the license
 * the number of users allowed by the current license
 * the expiration date of the license
 * license type, such as `commercial` or `academic`

## Testing license enforcement

You can test licensing-related behavior of your add-on in a local, development environment to an
extent. But there's no way to replicate the interaction with the UPM and Atlassian Marketplace in a
local environment alone. In the production environment, the Atlassian Marketplace serves licenses
for new subscribers, and the application interacts with the Marketplace to get the license state.

