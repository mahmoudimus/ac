# Understanding Atlassian OnDemand

Atlassian OnDemand is Atlassian's hosted versions of JIRA, Confluence and other software. Many
customers prefer to buy Atlassian tools as a service because Atlassian handles maintenance,
hosting, upgrades and more. OnDemand instances of JIRA, Confluence, and Bamboo are referred 
to as JIRA Cloud, Confluence Cloud, and Bamboo Cloud.

Atlassian Connect is built to integrate with our OnDemand applications, so it's important 
to have a basic understanding how OnDemand works.

## Architecture
* Each JIRA and Confluence Cloud account is a separate instance of that product. Although an OnDemand
instance offers a unified product feel, the actual JIRA and Confluence servers are individual applications
running in separate isolated JVMs, that communicate with each other only over HTTP.
* Although each application is isolated from a security perspective, the underlying hardware resources, such as
hardware, network, CPU and memory may be shared between many customers.
* Each instance of JIRA or Confluence can be identified by its tenant ID. An instance URL is liable
to change without warning.
* Each instance of JIRA or Confluence has a set of users.
* Email addresses are unique within an instance, but may be used across multiple instances.
* You should identify users by key, rather than name or email. Keys are also unique within an
instance but may not be across instances.
* OnDemand admins will install your add-on by registering your descriptor into an instance. That
makes your add-on available to all users of that instance. You will not receive any communication from an
instance that does not have your descriptor installed, and you will not be able to communicate with them.
* Atlassian OnDemand only serves content over HTTPS. Your add-on must also only request content via
HTTPS.
* Only descriptors with a base url that starts with HTTPS are installable in OnDemand servers.
* No Atlassian OnDemand user can log in as a sysadmin, and your add-on cannot access any
functionality that is reserved for sysadmins. Only Atlassian can access sysadmin-level functionality.
* Atlassian OnDemand supports the following browsers: [JIRA](https://confluence.atlassian.com/display/JIRA/Supported+Platforms),
[Confluence](https://confluence.atlassian.com/display/JIRA/Supported+Platforms).
* Atlassian OnDemand supports these [languages](https://confluence.atlassian.com/display/AOD/Language+Support+in+Atlassian+OnDemand).
* Atlassian OnDemand servers are physically located in the US. Read more [here](https://www.atlassian.com/hosted/security).
* Atlassian OnDemand [FAQ](https://confluence.atlassian.com/display/AOD/Atlassian+OnDemand+FAQ)

#### Therefore, your add-on should:
* Store user data against an identifier combined from tenant id and user key
* Only serve content to OnDemand [via HTTPS](../developing/installing-in-ondemand.html)
* Test and support the supported browsers
* Use the localization parameters with each request to serve content in the appropriate language
* Publish a security statement about your own data storage practices

# Software upgrades and system restarts
* Every OnDemand instance restarts during a maintenance window as described in the
[OnDemand Maintenance Windows](https://confluence.atlassian.com/display/AOD/Atlassian+OnDemand+maintenance+windows) page.
* The weekly releases may or may not contain updates to JIRA, Confluence or other components.
Generally JIRA and Confluence update their versions every other week. If a product does not
contain an update, it may still be restarted.
* Individual OnDemand instances occasionally restart outside of that maintenance window to recover
from errors or to facilitate support.
* During either kind of restart, your add-on may choose to receive a webhook from each instance that
your add-on is installed on.
* The first request to each instance after a restart may have higher latency as caches are repopulated.

#### Therefore, your add-on should:
* Be resilient to an OnDemand instance being slow or temporarily unavailable.

## Purchasing & Licensing
* When an OnDemand customer selects a new product or add-on they are automatically entered into a
trial period.
* The trial period lasts for 30 days + the time until your next bill. So the actual trial period
will be between 31 days and 61 days, with an average of 45 days.
* OnDemand customers can choose to subscribe to products and add-ons on a monthly or annual basis
* OnDemand customers may cancel their accounts, or cancel individual products or add-ons. Cancelled
accounts remain valid and active until the end of the billing period.
* OnDemand customer data is removed fifteen days after cancellation. You should publish your own data
retention policy as described [in the FAQ](../resources/faqs.html).
* Add-on installation and add-on licensing are separate concerns. It is possible for an OnDemand
instance to have your add-on descriptor installed but not have a valid license. You should always
check the [license status](../concepts/licensing.html) on each request and serve an appropriate
response.

#### Therefore, your add-on should:
* Always check the license parameter on each request and observe the appropriate restrictions.
* Publish your own [data retention policies](../resources/faqs.html).
