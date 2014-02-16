# Atlassian Connect FAQ for P2 developers

### What does Atlassian Connect mean for a developer selling a Plugins 2 add-on today?
Traditional Plugins 2 add-ons will continue to work on-premises. Atlassian still has a large and
growing customer base for behind the firewall products. Each vendor can decide how to allocate
resources between an existing Plugins 2 add-on and a new Atlassian Connect add-on. In many cases,
code may be sharable between the two deployment models. Using modern web techniques, REST,
JavaScript, and front-end coding can encourage this.

### Should I maintain different add-ons for OnDemand and on-premises?
We expect that most current vendors will start by writing a new Atlassian Connect add-on for
OnDemand while maintaining their current Plugins 2 add-on for on-premises. In the future, vendors
will be able to sell Atlassian Connect add-ons in OnDemand and to on-premises customers. We hope
that over time, most vendors will transition fully to the Atlassian Connect model. This has two
significant advantages:

- You can address the large majority of our customers regardless of their deployment model - Your
add-on will be much less coupled to the host product, making it more resilient and your customers
more likely to upgrade successfully

### What does this mean for a vendor whose Plugins 2 add-on is currently bundled in OnDemand?
You can and should start selling your add-on through the Atlasssian Marketplace today. To do so, you
should implement Atlassian [licensing](../concepts/licensing.html) in your add-on and submit a new
version. As the Atlassian OnDemand platform matures, our goal is to transition all third-party
Plugins 2 add-ons to Atlassian Connect. The security and robustness that the new platform provides
will help both Atlassian and vendors to move forward more quickly. We will work with you
individually to accomplish this over the coming years.

### Are Atlassian developers going to use Atlassian Connect?
Yes. Atlassian-developed add-ons will be taking advantage of the same sandboxed UIs and remote APIs
that are the core components of Atlassian Connect. We recognize that making use of these will make
our add-ons more decoupled and help increase the value of the platform for everyone. However, some
Atlassian add-ons will continue to run in-process in OnDemand and on-premises.

### How does Atlassian Connect relate to Application Links or UAL?
Atlassian Connect uses Application Links to store the relationship between the Atlassian application
and the external add-on which includes authentication information. This means that an Atlassian
Connect add-on appears as a regular Application Link when viewed through the Application Links
administration UI. Atlassian Connect can allow third-parties to provide a "one-click" user
experience for customers that want to enable the integration.
