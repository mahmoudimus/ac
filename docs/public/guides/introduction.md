# Introduction to Atlassian Connect
Build, install and sell add-ons in Atlassian OnDemand. Integrate your service, use Atlassian's REST APIs, or add new
features to Atlassian's world class development tools with Atlassian Connect.

## What is an Atlassian Connect add-on?
An Atlassian Connect add-on is any web application that extends an Atlassian application. It may be an existing
app that you integrate with the Atlassian app or a new service that you create to add features
to an Atlassian app. Atlassian Connect add-ons operate remotely over HTTP and can be written with any programming
language and web framework.

Fundamentally, Atlassian Connect add-ons have three major capabilities. Add-ons can:

1. Insert content in [certain defined places](../modules) in the Atlassian application's UI.
2. Make calls to the Atlassian application's [REST API](../rest-apis/product-api-browser.html).
3. Listen and respond to [WebHooks](../modules/jira/webhooks.html) fired by the Atlassian application.


## What does an Atlassian Connect add-on do?
- **Declare itself with a plugin descriptor.** The [add-on descriptor](../modules) is a JSON file that tells the application about the
add-on. Among other information, it tells the Atlassian application about the add-on's location and what features it
wants to provide. Administrators install Atlassian Connect add-ons into Atlassian applications by installing this descriptor file.
- **Define what add-on modules it wants to provide.** The features that an add-on can use within the Atlassian application are
called modules. For example, there are modules corresponding to: Macros in Confluence, Issue Panels in JIRA, or Pages.
Explore the [JIRA Modules](../modules/jira) and [Confluence Modules](../modules/confluence) sections of this documentation for reference information
specific for each application.
- **Request the appropriate scopes.** Your add-on must specify what type of access it needs from its host Atlassian
application. You declare required scopes for the add-on in the add-on descriptor file. Scopes determine which REST API
resources the add-on can use.
- **List in the Atlassian Marketplace.** Not every add-on needs to be publicly available on the [Atlassian
Marketplace](https://marketplace.atlassian.com), but all must be registered on the Marketplace in order to be installed. The Marketplace allows your to create
private listings, which accessible only to you.
- **Add user interface elements to the Atlassian application UI.**
- **Recognize the user.** Because your add-on has been authenticated via JWT, each request from the target application
to your add-on contains details about the user currently viewing that page. This allows you to serve the right context,
respect necessary permissions and make other decisions based on the user's identity.
- **Respond to the Atlassian application's webhooks.** By registering a webhook, your add-on can receive a notification when
certain events occur (for example, when a JIRA issue changes status). The webhook payload contains information about the
event, allowing your add-on to respond appropriately.
- **Call the application's remote API.** Your add-on can call the application's API via REST. You can use this to retrieve
data (for example, to build a report) or to push information into the target application (for example, to create an issue
in response to an external event).
- **Respect add-on licensing.** Every request from the Atlassian application to your add-on contains the add-on license
status for that instance. Your add-on can respond appropriately, for example, by alerting the user, locking down
functionality, or encouraging an upgrade.


## Interacting with Atlassian OnDemand
From the Atlassian application instance's perspective, the add-ons are software-as-a-service. To an
end user, the add-on appears as a fully integrated part of the Atlassian application. After subscribing to the add-on,
the features are delivered from within the UI and workflows of the host application.

Most Atlassian Connect add-ons will be implemented as multi-tenanted services. This means that a
single Atlassian Connect application must take into account multiple subscribing organizations. For example, each add-on
will maintain subscriber-specific data and configuration. For more about multi-tenancy design considerations, see
[Add-on Design Considerations](https://developer.atlassian.com/display/AC/Add-on+Design+Considerations).

<img src="../assets/images/DocDiagram.png" alt="Deployment architecture" />

Security is a important concern in a distributed component model such as Atlassian Connect. Atlassian Connect relies on
HTTPS and JWT authentication to secure communication between the add-on, the Atlassian product instance and the
end-user's browser.

Further, the add-on's actions in the context of the application are constrained by well-defined permissions. The add-on
can only perform activities it declares in its descriptor, and which are accepted by the administrator. These include,
for example, permissions governing reading content, creating pages, creating issues, and more. This helps to ensure the
security and stability of the OnDemand instance.

Read our [security overview](../concepts/security.html) for more details.

## About this guide
This guide is written for any developer who wants to create Atlassian Connect add-ons to extend Atlassian OnDemand or
installable applications.

To get the most out of this information, you should be familiar with:

- Using and administering Atlassian applications
- Web programming, whether using Java or another language
- Web server concepts and administration
- Web security standards, such as JWT
- Interacting with REST APIs



