# Selling on the Atlassian Marketplace

[Installing in the cloud](./cloud-installation.html) outlines how to install and test your add-on 
into your cloud-hosted product using a Marketplace listing. To **sell** your add-on, your listing 
will need to meet our [approval guidelines](https://developer.atlassian.com/x/WQaf#Add-onapprovalguidelines-AdditionalcriteriaforAtlassianConnectadd-ons) 
and be listed publicly. Public add-ons are available to any cloud customers, whether your add-on is free 
or paid via Atlassian. 

## How customers purchase Connect add-ons

Customers can subscribe to Connect add-ons from their cloud-hosted Atlassian products, like JIRA 
Cloud or Confluence Cloud. After a 30-day free trial, customers begin paying the subscription fee 
that you set. 

Free trials automatically roll into paid subscriptions (if your add-on is paid, of course), until 
the customer cancels their subscription. 
- Cloud customers can subscribe to your Connect add-on. After a 30-day trial period, the customer
will begin paying the subscription fee that you set.
- Atlassian Connect add-on trials automatically roll into a subscription, and then roll from one
month into another unless cancelled.
- If the administrator cancels a trial or unsubscribes from the add-on, the add-on may remain
installed in the application until the add-on descriptor is removed. Be sure to check the license
attribute on all incoming requests from the Atlassian application.

Keep in mind that installation and licensing are separate functions for Atlassian Connect add-ons.
Thus, an add-on with an expired license can retain a presence in the UI and retain data generated
while active. Every request made to your add-on includes the license status for that particular
instance. It is up to you to take appropriate action for a request where the license state is not
valid. See the [licensing documentation](../concepts/licensing.html) for details.

For more about how administrators install and manage add-ons, see the
[Universal Plugin Manager documentation](https://confluence.atlassian.com/display/UPM/About+the+Universal+Plugin+Manager).

## Making your listing public

Before anything else, make sure your add-on meets our [approval guidelines](https://developer.atlassian.com/x/WQaf#Add-onapprovalguidelines-AdditionalcriteriaforAtlassianConnectadd-ons). You might also find 
[Marketplace documentation](https://developer.atlassian.com/x/KYBpAQ) helpful, especially the 
[FAQs](https://developer.atlassian.com/x/PIFpAQ). 

When you're ready to list your add-on for the world to see, follow the process below and choose 
**Public** for your add-on visibility. 

<span data-include="/assets/includes/mpac-listing-instructions.html"></span>

After you've gone through the steps above, we automatically create a JIRA issue for our Marketplace 
team. We'll check your add-on and listing information agains our [approval guidelines](https://developer.atlassian.com/x/WQaf#Add-onapprovalguidelines-AdditionalcriteriaforAtlassianConnectadd-ons). 
This process might take a few business days. Avoid unnecessary rounds of feedbacy by checking the 
approval guidelines before submission. 
