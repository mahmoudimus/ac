# What is Atlassian Connect?
Atlassian Connect is a development framework for building add-ons to Atlassian's applications,
 like JIRA, Confluence, and HipChat. An add-on could be an integration with another existing service, 
 new features for the Atlassian application, or even a new application using the Atlassian app as a platform.

## What is an Atlassian Connect add-on?
Simply understood, Atlassian Connect add-ons are web applications.
 Atlassian Connect add-ons operate remotely over HTTP and can be written with any programming
language and web framework.

Fundamentally, Atlassian Connect add-ons have three major capabilities:

1. Insert content in [certain defined places](../modules/jira/index.html) in the Atlassian application's UI.
2. Make calls to the Atlassian application's [REST API](../rest-apis/product-api-browser.html).
3. Listen and respond to [WebHooks](../modules/jira/webhooks.html) fired by the Atlassian application.


## What does an Atlassian Connect add-on do?
- **Declare itself with a plugin descriptor.** 
The [add-on descriptor](../modules) is a JSON file that tells the Atlassian application about the add-on. 
In the descriptor, an add-on declares where it is hosted, 
which modules it intends to use, and metadata about the add-on.
- **Extend the Atlassian application UI with modules.** 
The features that an add-on can use within the Atlassian application are
called modules. 
There are modules for [general pages](../modules/jira/general-page.html) in the application 
or more specific locations, like [panels](../modules/jira/web-panel.html) in JIRA issues 
or [macros](../modules/confluence/dynamic-content-macro.html) in Confluence pages. 
Refer to the full module lists for [JIRA](../modules/jira/index.html) 
and [Confluence](../modules/confluence/index.html).
- **Request appropriate scopes.** Your add-on must specify what type of access it needs from the Atlassian
application. You declare required scopes for the add-on in the add-on descriptor file. Scopes determine which REST API
resources the add-on can use.
- **Recognize the user.** Atlassian Connect add-ons [authenticate users](../modules/authentication.html) via JWT, 
so each request from the Atlassian application to your add-on will contain
details about the user currently viewing that page. 
This allows you to serve the right context,
respect necessary permissions and make other decisions based on the user's identity.
- **Call the Atlassian application's REST API.** Your add-on can retrieve
data (for example, to build a report) or 
push information to the Atlassian host application 
(for example, to create an issue in response to an external event). 
- **Respond to the Atlassian application's webhooks.** With [webhooks](../modules/jira/webhooks.html), 
your add-on can receive a notification when
certain events occur (for example, when a JIRA issue changes status). 
The webhook payload contains information about the
event, allowing your add-on to respond appropriately.
- **Register on the Atlassian Marketplace.** Your add-on does not need to be publicly available on the [Marketplace](https://marketplace.atlassian.com),
but it must be registered on the Marketplace in order to be installed. 
The Marketplace supports private listings which are only accessible with secret tokens that you generate.
- **Respect add-on licensing.** Every request from the Atlassian application to your add-on contains the add-on license
status for that instance. Your add-on can respond appropriately, for example, by alerting the user, locking down
functionality, or encouraging an upgrade.


## How does Atlassian Connect work?
To an end user, your add-on should appear 
as a fully integrated part of the Atlassian application.
Once your add-on is registered with the application,
features are delivered from the UI and workflows of the host application. This deep level of integration is part of what makes 
Atlassian Connect add-ons so powerful.

<div id="architecture-graphic"></div>

### Architecture
Most Atlassian Connect add-ons are implemented as multi-tenanted services. This means that a
single Atlassian Connect application will support multiple subscribing Atlassian applications. For more information, see  multi-tenancy design considerations, 
see [Understand OnDemand](../concepts/understanding-ondemand.html).

###Security
Security is critical in a distributed component model such as Atlassian Connect. Atlassian Connect relies on
HTTPS and JWT authentication to secure communication between your add-on, the Atlassian product and the user.

Your add-on's actions are constrained by well-defined permissions. 
Your add-on can only perform activities it declares in its descriptor. These permissions are granted by the Atlassian application administrator
when she installs your add-on. Examples of permissions include
reading content, creating pages, creating issues, and more. 
These permissions help ensure the
security and stability of the OnDemand instance.

Read our [security overview](../concepts/security.html) for more details.

###Design
Since Atlassian Connect add-ons can insert content directly
into the Atlassian host application, it is critical that add-ons
are visually compatible with the Atlassian application's design.
To help developers, Atlassian's design team has created detailed 
[desisgn guidelines](https://developer.atlassian.com/design/latest/)
and a [library of reusable front-end UI components](https://docs.atlassian.com/aui/latest/).

##Let's get started
If you made it this far, you're ready to write your first Atlassian Connect add-on! Follow the link below to get started.
<div class="index-button">
<a href="../guides/getting-started.html"><button class="primary-cta aui-button aui-button-primary">Hello, world!</button></a>
</div>