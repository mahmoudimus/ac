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
Atlassian Connect is available in JIRA and Confluence Cloud for development and use.

Atlassian Connect currently supports:

- JIRA Cloud
- Confluence Cloud

Other products, including JIRA and Confluence Server will be supported in the future.

### How can I request new features for Atlassian Connect?
If there's a feature you'd like to see added to Atlassian Connect, such as a new module type or a
particular REST method, please let us know. Submit new feature requests, bugs, and feature votes in
the Atlassian Connect JIRA project:

[https://ecosystem.atlassian.net/browse/AC](https://ecosystem.atlassian.net/browse/AC)

### Can an Atlassian OnDemand customer use Atlassian Connect for custom development?
Yes. Every Atlassian OnDemand customer has access to the Atlassian Connect platform. This means
that internal developers can create and deploy Atlassian Connect add-ons for their own use
within their organization. For example, a developer might integrate JIRA Cloud with an internal
system, or to integrate Confluence Cloud with another SaaS service's API.

Note that add-ons in OnDemand must be installed through the Atlassian Marketplace. Thus, to
install an add-on for internal use, you must create a listing for it on the Marketplace. The
Marketplace provides a new type of listing for such cases - private listings. A private listing is
not publicly visible on the Marketplace, and can be installed on a maximum of fifty OnDemand
instances.

### How does Atlassian Connect work?
An Atlassian Connect add-on is simply a web application that describes itself with an Atlassian
Connect descriptor file. That descriptor includes authentication settings and declares the add-on's
capabilities. Capabilities take the form of modules. A module specifies an HTTP
resource exposed by the add-on and the place where that resource interacts with the Atlassian app.

### What languages, frameworks, & hosts will be supported
Because your remote add-on is decoupled from the Atlassian application, using only HTTP and
REST to communicate, you are free to build in any language, use any framework, and deploy in any
manner you wish.

### Will Atlassian provide hosting for add-ons?
No. You may choose from the many great PaaS or hosting providers.

### How are Atlassian Connect add-ons installed?
Add-ons are listed on the Atlassian Marketplace and installed via the Add-on manager in every
Atlassian application.

### How are Atlassian Connect add-ons supported by Atlassian?
Atlassian Connect add-ons receive the same level of support that traditional add-ons do today. Atlassian
supports the platform, the SDK and the documentation. Vendors are responsible for supporting the
add-ons they build and the customers who use those add-ons.

### How should add-on vendors support their customers?
Vendors must provide a support channel when listing on the Marketplace. That support channel should
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

### What policies must add-ons observe about customer data?

As an Atlassian Connect developer, you must be responsible with the data entrusted to you by your
customers. Atlassian Connect developers must create and display a Data Security & Privacy Statement
and include that in your Marketplace Listing. Including simple and easily described information
about your service in your Data Security and Privacy Statement will reassure your customers that you
are acting as a professional and trustworthy provider of hosted software.

For reference, here are Atlassian's relevant policies:

* [Atlassian OnDemand Security Statement](https://www.atlassian.com/hosted/security)
* [Atlassian OnDemand Storage Policy](https://confluence.atlassian.com/display/AOD/OnDemand+Storage+Policy)
* [Atlassian OnDemand Data Policy](https://confluence.atlassian.com/display/AOD/About+Your+Data)
* [Atlassian's Privacy Policy](https://www.atlassian.com/company/privacy)
* [Atlassian's Security Advisory Policy](https://confluence.atlassian.com/display/Support/Security+Advisory+Publishing+Policy)

Your policy may cover the following areas:

* **Data storage and location:** Explain where your application will store data from your customers
and where (physically) the data will be stored. It is your responsibility to comply with all local
laws.
* **Backups:** Explain your backup and recovery policy for customer data. You should publish your
[RTO](http://en.wikipedia.org/wiki/Recovery_time_objective) and [RPO](http://en.wikipedia.org/wiki/Recovery_point_objective)
targets, and explain if and when data is moved offsite. For Atlassian OnDemand, backups are made daily, and stored
offsite on a weekly basis.
* **Account removal and data retention:** Explain how a customer can close an account and completely
remove their data from your service. For Atlassian OnDemand, customer data is retained for 15 days
after account removal and then unrecoverably deleted after that time.
* **Data portability:** Explain if and how a customer can extract their data from your service. For
example, is it possible to move from your hosted service to a downloaded version of your software?
* **Application and infrastructure security:** Explain what security measures you've taken in your
application and infrastructure, for example on-disk data encryption or encrypted data transfer between servers.
* **Security disclosure:** Explain how and under what circumstances you would notify customers about
security breaches or vulnerabilities. You should also indicate how a user or security researcher should
disclose a vulnerability found in your add-on to you. (Example from Atlassian:
[How to report a security issue](https://confluence.atlassian.com/display/DOC/How+to+Report+a+Security+Issue))
* **Privacy:** Explain that data collected during the use of your add-on will not be shared with
third parties except as required by law.

### How can add-ons change code safely?
Atlassian Connect is designed to decouple add-ons from the Atlassian application. Because you are
running a remote service, you can change your underlying application at any time and as often as you
find necessary. The only part of your application controlled by Atlassian is your add-on descriptor
file, which is stored on the [Atlassian Marketplace](https://marketplace.atlassian.com/). You can
change your descriptor by deploying a new version. The Marketplace will recognize the new descriptor
and it will automatically be pushed to all your customers. By versioning your API, paying attention
to the versioning and careful deprecation of the Atlassian application's API, you can move forward
with more confidence.

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
