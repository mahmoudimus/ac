# Understanding Atlassian in the cloud

Atlassian Connect is built to integrate with our hosted applications like JIRA and Confluence Cloud. 
This page explains basic architecture, purchasing, licensing, and development concerns as you build 
your add-on. 

Covered here: 

* [Architectural overview of Atlassian cloud products](#overview) 
* [Cloud application updates & restarts](#restarts) 
* [Purchasing & licensing](#purchasing) 
* [Development checklist](#development) 

## <a id="overview"></a>Architecture overview

Even though cloud products can be packaged together for customers, each JIRA and Confluence Cloud account 
is a separate instance. The actual JIRA and Confluence servers are individual applications
running in separate, isolated JVMs, communicating only over HTTP. Our cloud products only serve content over 
[HTTPS](../developing/cloud-installation.html). 

Although each application is isolated from a security perspective, underlying resources like hardware,
CPU and memory can be shared between many customers. Servers for our cloud applications are [located in the US](https://www.atlassian.com/hosted/security). 

Each JIRA or Confluence Cloud instance is identifiable by its tenant ID. An instance URL is liable to change 
without warning. 

Each cloud instance has a set of licensed users. For these users:

* __Email addresses are unique within an instance, but may be used across multiple instances.__  
* __Users are identified by key, rather than name or email.__ Keys are also unique within an
	instance but may not be across instances. 
* __No user can log in as a sysadmin, and your add-on cannot access any functionality reserved for sysadmins.__ 
	Only Atlassian can access sysadmin-level functionality.  


Your add-on automatically creats an add-on user in cloud instances. These "users" appear in the 
user management portal, but don't count against actual user licenses. This user profile is assigned to two 
groups by default: _atlassian-addons_ and _product-users_ (like _jira-users_ or _confluence-users_). Customers 
should **not** remove add-on users from these groups.  

Your add-on accesses cloud instances through the [Universal Plugin Manager](https://confluence.atlassian.com/x/8AJTE). Admins install your add-on by registering your descriptor into an instance. Add-on installation and licensing are separate concerns. 

It's possible for a cloud instance to have your descriptor installed, but not to have a valid license. 

You won't receive any  communication from instances that don't have your add-on descriptor installed. As 
expected, you're unable to communicate with instances that don't have your decriptor installed. 

## <a id="restarts"></a>Software upgrades and system restarts

Cloud instances restart regularly during [maintenance windows](https://confluence.atlassian.com/x/aJALE). 
Weekly releases may or may not contain updates to cloud applications or other components. Generally, 
JIRA and Confluence update versions every other week. That said, even if a product doesn't have an 
update, it may still be restarted. 

Instances also ocassionally restart outside of these windows to recover from errors, or facilitate 
support. After restarting, initial requests to these instances may have higher latency as caches 
are repopulated. 

## <a id="purchasing"></a>Purchasing & licensing  

When cloud product customers choose a new product or add-on, they automatically enter a free 
trial period. This trial lasts 30 days, plus the time until their next bill. This means the 
actual trial period is between 31 and 61 days, with an average of 45 days. 

Customers can choose to subscribe to producs and add-ons on a monthly or annual basis, and can 
cancel accounts or add-ons. Cancelled accounts remain valid and active until the end of the billing 
period. 

We remove cloud product data 15 days after cancellation. For this reason, publish your own data retention 
policy. 
 
Since add-on installation and licensing are handled separately, always check the 
[license status](../concepts/licensing.html) on each request and serve an appropriate
response.

## <a id="development"></a>Development checklist

Develop your add-on with the above concepts in mind: 

* Ensure your descriptor's base URL starts with HTTPS. 
* Only serve content over HTTPS. 
* Test and support your add-on for cloud application-supported browsers.
* Use  localization parameters with each request to serve content appropriate languages. 
* Publish a security statement about your data storage practices.
* Store user data against an identifier combined from tenant ID and user key. 
* Publish your own [data retention policies](../resources/faqs.html).
* Develop your add-on to be resilient when cloud instances are slow or unavailable. 
* Always check the license parameter on each request and observe the appropriate restrictions.

See the following pages for more details:

* [Supported platforms for JIRA](https://confluence.atlassian.com/x/qgReD)  
* [Supported platforms for Confluence](https://confluence.atlassian.com/x/xgReD)  
* [Supported languages for cloud applications](https://confluence.atlassian.com/x/fTIvEw)
* [Cloud application FAQ for users](https://confluence.atlassian.com/x/9QEYDw)
