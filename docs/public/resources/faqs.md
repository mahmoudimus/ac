# Atlassian Connect FAQ

### What is Atlassian Connect?
Atlassian Connect is a distributed add-on technology for extending Atlassian applications such as
JIRA and Confluence. Atlassian Connect is built for a world where software runs wherever, whenever,
and however. Atlassian Connect add-ons extend Atlassian applications entirely over standard web
protocols and APIs, such as HTTP and REST. This frees developers from traditional add-on platform
constraints, giving them new choices of programming language and deployment options. Regardless of
delivery model or location, Atlassian applications can be extended with Atlassian Connect add-ons,
so developers can be confident their add-ons can solve anyone's business problem.

### Where are the docs?
You've found them!

The primary documentation for Atlassian Connect [is here](../index.html). Individual tools that you
can use with Atlassian Connect, such as
[atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express), may provide
additional documentation.

Additionally, the Atlassian Connect framework docs are also available _in your Atlassian product._
Just visit `https://HOSTNAME:PORT/CONTEXT_PATH/atlassian-connect/docs/`. This will display the
documentation for the version of Atlassian Connect that is currently running in your product, so the
documentation is always guaranteed to be in sync.


### What other information resources exist?
- [Google group](https://groups.google.com/forum/?fromgroups#!forum/atlassian-connect-dev)
- [JIRA issue tracker for Atlassian Connect](https://ecosystem.atlassian.net/browse/AC)
- [Atlassian Connect on Atlassian Answers](https://answers.atlassian.com/tags/atlassian-connect)

### What products support Atlassian Connect?
Atlassian Connect is available in JIRA and Confluence OnDemand for beta development and use.

Atlassian Connect currently supports:

- JIRA OnDemand
- Confluence OnDemand

Other products, including JIRA and Confluence Download will be supported in the future.

### How can I request new features for Atlassian Connect?
If there's a feature you'd like to see added to Atlassian Connect, such as a new module type or a
particular REST method, please let us know. Submit new feature requests, bugs, and feature votes in
the Atlassian Connect JIRA project:

[https://ecosystem.atlassian.net/browse/AC](https://ecosystem.atlassian.net/browse/AC)

### Can an Atlassian OnDemand customer use Atlassian Connect for custom development?
Yes. Every Atlassian OnDemand customer has access to the Atlassian Connect platform. This means
that internal developers can create and deploy Atlassian Connect add-ons for their own use
within their organization. For example, a developer might integrate JIRA OnDemand with an internal
system, or to integrate Confluence OnDemand with another SaaS service's API.

Note that add-ons in OnDemand must be installed through the Atlassian Marketplace. Thus, to
install an add-on for internal use, you must create a listing for it on the Marketplace. The
Marketplace provides a new type of listing for such cases - private listings. A private listing is
not publicly visible on the Marketplace, and can be installed on a maximum of ten OnDemand
instances.

### How does Atlassian Connect work?
An Atlassian Connect add-on is simply a web application that describes itself with an Atlassian
Connect descriptor file. That descriptor includes authentication settings and declares the add-on's
capabilities. Capabilities take the form of modules. A module specifies an HTTP
resource exposed by the add-on and the place where that resrouce interacts with the Atlassian app.

For example, a webhook module requires a URL that can accept a webhook that is sent from the host
application. A general-page capability requires a URL on the remote site that can return HTML to be
displayed in the host application. Atlassian Connect is ideal for an external site that wants to
provide integration into an Atlassian application no matter what language that external service is
written in.

### What languages, frameworks, & hosts will be supported
Because your remote add-on is decoupled from the Atlassian application, using only HTTP and
REST to communicate, you are free to build in any language, use any framework, and deploy in any
manner you wish.

### Will Atlassian provide hosting for add-ons?
No. You may choose from the many great PaaS or hosting providers.

### How are Atlassian Connect add-ons installed?
Add-ons are listed on the Atlassian Marketplace and installed via the Add-on manager in every
Atlassian application.

### How does Atlassian Connect relate to Application Links or UAL?
Atlassian Connect uses Application Links to store the relationship between the Atlassian application
and the external add-on which includes authentication information. This means that an Atlassian
Connect add-on appears as a regular Application Link when viewed through the Application Links
administration UI. Atlassian Connect can allow third-parties to provide a "one-click" user
experience for customers that want to enable the integration.

### To what extent are Atlassian Connect add-ons supported by Atlassian?
Atlassian Connect add-ons receive the same level of support that Plugins 2 add-ons do today. Atlassian
supports the platform, the SDK and the documentation. Vendors are responsible for supporting the
add-ons they build and the customers who use those add-ons.

### How should add-on vendors support their customers?
Vendors must provide a support channel when listing on the marketplace. That support channel should
be an issue tracker or ticketing system where a customer can file and track issues. An email address
is insufficient. We can provide a JIRA instance for vendors who wish to use it for support and issue
tracking. Atlassian believes in a policy of transparency, and that information should be open by
default. As such, we encourage (but do not require) you to make your tracker open to the public.

In the future, there may be SLAs around support tickets for some or all vendors.

### What are the support requirements for add-on vendors relative to Atlassian?
If Atlassian files a support ticket in your system, we ask for next-business-day response time, and
resolution time as quickly as possible. We reserve the right to disable your plugin and remove it
from the Marketplace if problems cannot be resolved.

### What are the service requirements for an add-on?
There are currently no service-level agreements enforced for add-ons in the Atlassian Marketplace.
However, in OnDemand, the service level is very important to customers. We intend to measure each
add-on's current status and uptime and make that information available to customers, similar to the
way that [OnDemand does so](https://www.atlassian.com/software/ondemand/status). We encourage
add-on providers to strive for 99.9% uptime.

### How can add-ons change code safely?
Atlassian Connect is designed to decouple add-ons from the Atlassian application. Because you are
running a remote service, you can change your underlying application at any time and as often as you
find necessary. The only part of your application controlled by Atlassian is your add-on descriptor
file, which is stored on the [Atlassian Marketplace](https://marketplace.atlassian.com/). You can
change your descriptor by uploading a new version to the Marketplace, which will automatically be
pushed to all your customers. By versioning your API, paying attention to the versioning and careful
deprecation of the Atlassian application's API, you can move forward with more confidence.

See [Upgrades](../developing/upgrades.html) for more details.

### What does this mean for a new add-on developer?
We expect that new developers - both commercial and internal - can start with Atlassian Connect.
They will use sandboxed UIs and remote APIs, which provide much more stability over time. If you are
integrating Atlassian tools with another service or remote application, this is the ideal path for
development.

If a developer wants to create an add-on that's more deeply intertwined with the target application
and does not intend to ever make it available to OnDemand customers, then [Plugins 2
add-ons](https://developer.atlassian.com/display/DOCS/Getting+Started) will continue to be supported
for development.

### What happens when a customer moves from OnDemand to on-premises or vice versa?
One of the great strengths of the Atlassian platform is that customers can choose the deployment
model that best suits their needs. We often see customers move from OnDemand to on-premises and vice
versa, as their needs change. When a customer using one of your Atlassian Connect add-ons needs to
move between deployment models, they will be able to continue to use your add-on.

Atlassian sales will be able to transfer or issue new licenses as necessary. Developers will be
responsible for re-associating any data stored on your service with the customer's new hostname.


### See also [FAQS for P2 developers.](./faqs-for-p2-developers.html)