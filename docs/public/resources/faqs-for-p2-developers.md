# Atlassian Connect FAQ for P2 developers

### What does Atlassian Connect mean for a developer selling a Plugins 2 add-on today?
Traditional Plugins 2 add-ons will continue to work on-premises. Atlassian still has a large and
growing customer base for behind the firewall products. Each vendor can decide how to allocate
resources between an existing Plugins 2 add-on and a new Atlassian Connect add-on. In many cases,
code may be sharable between the two deployment models. Using modern web techniques, REST,
JavaScript, and front-end coding can encourage this.

### Should I maintain different add-ons for cloud and server instances?
We expect that most current vendors will start by writing a new Atlassian Connect add-on for
cloud products while maintaining their current Plugins 2 add-on for on-premises. We hope
that over time, most vendors will transition fully to the Atlassian Connect model. This has two
significant advantages:

- You can address the large majority of our customers regardless of their deployment model - Your
add-on will be much less coupled to the host product, making it more resilient and your customers
more likely to upgrade successfully

### What does this mean for a vendor whose Plugins 2 add-on is currently bundled in cloud products?
You can and should start selling your add-on through the Atlasssian Marketplace today. To do so, you
should implement Atlassian [licensing](../concepts/licensing.html) in your add-on and submit a new
version. As the Atlassian cloud platform matures, our goal is to transition all third-party
Plugins 2 add-ons to Atlassian Connect. The security and robustness that the new platform provides
will help both Atlassian and vendors to move forward more quickly. We will work with you
individually to accomplish this over the coming years.

### Are Atlassian developers going to use Atlassian Connect?
Yes. Atlassian-developed add-ons will be taking advantage of the same sandboxed UIs and remote APIs
that are the core components of Atlassian Connect. We recognize that making use of these will make
our add-ons more decoupled and help increase the value of the platform for everyone. However, some
Atlassian add-ons will continue to run in-process in cloud and server instances.
