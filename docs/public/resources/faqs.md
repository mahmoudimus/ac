# Atlassian Connect FAQ

### What is Atlassian Connect?
Atlassian Connect is a distributed add-on technology for extending Atlassian applications such as JIRA and Confluence. Atlassian Connect is built for a world where software runs wherever, whenever, and however. Atlassian Connect add-ons extend Atlassian applications entirely over standard web protocols and APIs, such as HTTP and REST. This frees developers from traditional add-on platform constraints, giving them new choices of programming language and deployment options. Regardless of delivery model or location, Atlassian applications can be extended with Atlassian Connect add-ons, so developers can be confident their add-ons can solve anyone's business problem. 

### Where are the docs?
You've found them!

The primary documentation for Atlassian Connect [is this space](./index.html). Individual tools that you can use with Atlassian Connect, such as [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express), may provide their own, separate documentation.

### What other information resources exist?
- [Google group](https://groups.google.com/forum/?fromgroups#!forum/atlassian-connect-dev)
- [JIRA issue tracker for Atlassian Connect](https://ecosystem.atlassian.net/browse/AC)
- [Atlassian Connect on Atlassian Answers](https://answers.atlassian.com/tags/atlassian-connect)

### What is the launch timeline for Atlassian Connect?
Beta of Atlassian Connect started in May 2013 at [AtlasCamp 2013](https://www.atlassian.com/atlascamp/2013) and was publicly announced in October 2013 at Summit 2013. We are expecting to announce a 1.0 release in the first quarter of 2014.

### What products support Atlassian Connect?
Atlassian Connect is available in JIRA and Confluence OnDemand for beta development and use.

Atlassian Connect currently supports:

- JIRA OnDemand
- Confluence OnDemand

At Summit 2014, we intend to support Atlassian Connect in behind-the-firewall JIRA and Confluence instances. And we hope to support Atlassian Connect in Bitbucket and HipChat as well.

### How can I request new features for Atlassian Connect?
If there's a feature you'd like to see added to Atlassian Connect, such as a new module type or a particular REST method, please let us know. Submit new feature requests, bugs, and feature votes in the Atlassian Connect JIRA project:

[https://ecosystem.atlassian.net/browse/AC](https://ecosystem.atlassian.net/browse/AC)

### Can an Atlassian OnDemand customer use Atlassian Connect for custom development?
Yes. Atlassian OnDemand customers have access to all of the Atlassian Connect technology. This means that internal developers can create and deploy Atlassian Connect add-ons for their own, sole use within their organization. For example, to integrate JIRA OnDemand with an internal system, or to integrate Confluence OnDemand with some other service their organization uses. 

Note that add-ons in OnDemand will be installable through the Atlassian Marketplace only. Thus, to install an add-on for internal use, you will still need to create a listing for it on the Marketplace. The Marketplace will provide a new type of listing for such cases - private add-on listings. A private listing is not publicly visible on the Marketplace. **Private listings can be installed on a maximum of ten OnDemand instances**.

### How does Atlassian Connect work?
An Atlassian Connect add-on is simply a web application that describes itself with an Atlassian Connect descriptor file. That descriptor includes authentication settings and declares the add-ons capabilities. Capabilities take the form of remote integration modules. A module specifies an HTTP resource exposed by the add-on that implements the integration.

For example, a webhook listener capability requires a URL that can accept a JSON document received as a POST message from the host application. A general-page capability requires a URL on the remote site that can return HTML to be displayed in the host application. Atlassian Connect is ideal for an external site that wants to provide integration into an Atlassian application no matter what language that external service is written in.

### What languages, frameworks, & hosts will be supported
Because your remote add-on is fully decoupled from the Atlassian application, using only HTTP and REST to communicate, you are free to build in any language, use any framework, and deploy in any manner you want.

### Will Atlassian provide hosting for add-ons?
No. You may choose from any of the great PaaS or hosting providers on the market today.

### How are Atlassian Connect add-ons installed?
Add-ons will be installable in OnDemand through the Atlassian Marketplace only. To install custom add-ons intended for the private use of an organization, you will need to create a private listing for the add-on on the Marketplace. 

### How does Atlassian Connect relate to Application Links or UAL?
Atlassian Connect uses Application Links to store the relationship between the Atlassian application and the external add-on which includes authentication information. This means that an Atlassian Connect add-on appears as a regular Application Link when viewed through the Application Links administration UI. Atlassian Connect can allow third-parties to provide a "one-click" user experience for customers that want to enable the integration.

### To what extent will Atlassian Connect add-ons be supported by Atlassian?
Atlassian Connect add-ons will receive the same level of support that Plugins 2 add-ons do today. We support the platform, the SDK and the documentation. Vendors are responsible for supporting the add-ons they build. 

### What are the support requirements for add-on vendors relative to their customers?
Vendors must provide a support channel when listing on the marketplace. That support channel should be an issue tracker or ticketing system where a customer can file and track issues. An email address is insufficient. We can provide a JIRA instance for vendors who wish to use it for support and issue tracking. Atlassian believes in a policy of transparency, and that information should be open by default. As such, we encourage (but do not require) you to make your tracker open to the public.

In the future, there may be SLAs around support tickets for some or all vendors.

### What are the support requirements for add-on vendors relative to Atlassian?
If Atlassian files a support ticket in your system, we ask for next-business-day response time, and resolution time as quickly as possible. We reserve the right to disable your plugin and remove it from the Marketplace if problems cannot be resolved.

### What are the service requirements for an add-on?
There are currently no service-level agreements enforced for add-ons in the Atlassian Marketplace. However, in OnDemand, the service level is very important to customers. We intend to measure each add-on's current status and uptime and make that information available to customers, similar to the way that [OnDemand does so here](https://www.atlassian.com/software/ondemand/status). We encourage add-on providers to strive for 99.9% uptime.

### What does Atlassian Connect mean for a developer selling a Plugins 2 add-on today?
Traditional Plugins 2 add-ons will continue to work on-premises. Atlassian still has a large and growing customer base for behind the firewall products. Each vendor can decide how to allocate resources between an existing Plugins 2 add-on and a new Atlassian Connect add-on. In many cases, code may be sharable between the two deployment models. Using modern web techniques, REST, JavaScript, and front-end coding can encourage this.

### Should I maintain different add-ons for OnDemand and on-premises?
We expect that most current vendors will start by writing a new Atlassian Connect add-on for OnDemand while maintaining their current Plugins 2 add-on for on-premises. In the future, vendors will be able to sell Atlassian Connect add-ons in OnDemand and to on-premises customers. We hope that over time, most vendors will transition fully to the Atlassian Connect model. This has two significant advantages:

- You can address the large majority of our customers regardless of their deployment model
- Your add-on will be much less coupled to the host product, making it more resilient and your customers more likely to upgrade successfully

### What does this mean for a vendor whose Plugins 2 add-on is currently bundled in OnDemand?
You can and should start selling your add-on through the Atlasssian Marketplace today. To do so, you should implement Atlassian [licensing](/concepts/licensing.html) in your add-on and submit a new version. As the Atlassian OnDemand platform matures, our goal is to transition all third-party Plugins 2 add-ons to Atlassian Connect. The security and robustness that the new platform provides will help both Atlassian and vendors to move forward more quickly. We will work with you individually to accomplish this over the coming years.

### How can add-ons change code safely?
Atlassian Connect is designed to decouple add-ons from the Atlassian application. Because you are running a remote service, you can change your underlying application at any time and as often as you find necessary. The only part of your application controlled by Atlassian is your add-on descriptor file, which is stored on the [Atlassian Marketplace](https://marketplace.atlassian.com/). You can change your descriptor by uploading a new version to the Marketplace, which will automatically be pushed to all your customers. By versioning your API, paying attention to the versioning and careful deprecation of the Atlassian application's API, you can move forward with more confidence.

### What does this mean for a new add-on developer?
We expect that new developers - both commercial and internal - can start with Atlassian Connect. They will use sandboxed UIs and remote APIs, which provide much more stability over time. If you are integrating Atlassian tools with another service or remote application, this is the ideal path for development. 

If a developer wants to create an add-on that's more deeply intertwined with the target application and does not intend to ever make it available to OnDemand customers, then [Plugins 2 add-ons](https://developer.atlassian.com/display/DOCS/Getting+Started) will continue to be supported for development. 

### Are Atlassian developers going to use Atlassian Connect?
Yes. Atlassian-developed add-ons will be taking advantage of the same sandboxed UIs and remote APIs that are the core components of Atlassian Connect. We recognize that making use of these will make our add-ons more decoupled and help increase the value of the platform for everyone. However, some Atlassian add-ons will continue to run in-process in OnDemand and on-premises.

### What happens when a customer moves from OnDemand to on-premises or vice versa?
One of the great strengths of the Atlassian platform is that customers can choose the deployment model that best suits their needs. We often see customers move from OnDemand to on-premises and vice versa, as their needs change. When a customer using one of your Atlassian Connect add-ons needs to move between deployment models, they will be able to continue to use your add-on.

Atlassian sales will be able to transfer or issue new licenses as necessary. Developers will be responsible for re-associating any data stored on your service with the customer's new hostname.

### What does the development environment look like?
You can develop an Atlassian Connect add-on using either a locally running Atlassian application or one hosted in Atlassian OnDemand. We predict that most developers will use different techniques during the development process. Developers can continue to launch instances of the product using the [Atlassian SDK](https://developer.atlassian.com/display/DOCS/Install+the+Atlassian+SDK+on+a+Windows+System). A developer can choose any version or milestone of the target application, including the exact build that is deployed to OnDemand. By running the application locally with a local instance of your add-on, you can develop quickly and easily, and without fussing with network setup.

Eventually, however, you will want to development in a true OnDemand instance. This involves making your application available on the web, either by hosting it or using a tool like [LocalTunnel](http://progrium.com/localtunnel/). You will register your add-on in Marketplace, in draft mode, and install it your OnDemand instance as described in [Getting Started](../guides/getting-started.html).

### How can I test Atlassian Connect in an OnDemand environment?
Add-on vendors can create a development instance without contacting Atlassian simply by trialing a standard Atlassian OnDemand instance. Before your thirty-day trial expires, send an email to [developer-relations@atlassian.com](mailto:developer-relations@atlassian.com) to get a developer license to OnDemand. We will automate this in the future.

The UPM exposes a developer configuration page that allows you to install add-ons from the Marketplace (even those in draft or pre-approval state). In addition, the developer configuration interface provides a method to set the license to the state you want to test: active, expired or none. This allows you to perform end-to-end license testing.