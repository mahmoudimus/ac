# Add-on Descriptor
The add-on descriptor is an JSON file (`atlassian-connect.json`) that describes the add-on to the Atlassian application.
If you're familiar with Java add-on development with previous versions of the Atlassian Framework, you should already be familiar with plugin descriptors.

The descriptor serves as the glue between the remote add-on and the Atlassian application. When an administrator for an Atlassian OnDemand instance subscribes to an add-on, the Atlassian instance retrieves the add-on descriptor from its published location. 

The descriptor includes general information for the add-on (in the `modules` element). It also declares the modules that the add-on wants to use or extend, as described in the following sections.

The `modules` element tells the application instance about the add-on. Among other things, the descriptor informs the instance about the name and description of the add-on and the modules it wants to implement.

Let's look at an example:

```
{
    "name": "My Addon",
    "description": "hello world description",
    "key": "com.example.tutorial.myaddon",
    "vendor": {
        "name": "Example, Inc.",
        "url": "https://www.example.com"
    },
    "baseUrl": "http://localhost:8000",
    "version": "1.0"
}
```

Omitted here are any module descriptors. We'll cover those in the [modules](modules.html) page.

Also, for details and application-specific reference information on the descriptor please refer to the "jira modules" and "confluence modules" sections of this documentation. But we'll call out a few highlights from the example here.

For the `description` value, you should supply a brief textual description of your add-on. When your add-on is installed in the Atlassian application, this information appears with the add-on in the Manage Add-ons page of the administration console. Thus, your description should provide meaningful and identifying information for the instance administrator. 

The version element identifies the version of the add-on itself. Note that versioning in general works a little differently in Atlassian Connect add-ons than it does in traditional, in-process add-ons.

Since Atlassian Connect add-ons are remote and largely independent from the Atlassian application, they can be enhanced or patched at any time, without having to report the change to the Atlassian instance. The changes are reflected in the Atlassian instance immediately (or at least at page reload time).

However, some add-ons changes do require a change in the descriptor file as well. For example, say you modify the add-on to have a new page module. Since this requires a page module declaration in the descriptor, it means making an updated descriptor available, which instances will have to re-register. To implement this change, you need to create a new version of the add-on in its Marketplace listing. The Marketplace and UPM will take care of the rest: informing administrators of the available update.

For more information about the permissions and license-related elements, see [Scopes](scopes.html) and [Licensing](licensing.html).
