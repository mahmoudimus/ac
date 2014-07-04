# Selling on the Atlassian Marketplace

[Installing in OnDemand](./cloud-installation.html) outlines how to install and test your add-on
into your OnDemand instance  using a Private Listing on the Marketplace.

To sell your Atlassian Connect add-on on the [Atlassian Marketplace](https://marketplace.atlassian.com/)
you must make that listing public. A publicly listed add-on is available to any OnDemand customer,
whether the add-on is free or paid via Atlassian.

With the listing already in place, to make the add-on public you usually only need to switch the
visibility setting for the listing and submit the add-on for approval.

## Background information
There are some general requirements and recommendations for selling on the Marketplace that apply to
both Atlassian Connect add-ons and traditional downloadable add-ons. A few places to get started are:

- [Atlassian Marketplace developer's site](https://developer.atlassian.com/display/MARKET/Marketplace+overview)
contains general information about selling add-ons (both Atlassian Connect and java add-ons)
on the Marketplace.
- [Add-on approval guidelines](https://developer.atlassian.com/display/MARKET/Add-on+approval+guidelines#Add-onapprovalguidelines-CriteriaforAtlassianConnectadd-ons).
- [Marketplace FAQ](https://www.atlassian.com/licensing/marketplace) contains customer-facing
information on the Marketplace.

## How customers purchase Atlassian Connect add-ons
The OnDemand try and buy flow for Atlassian Connect add-ons is similar to that of traditional
downloadable add-ons, but with a few differences. These include:

- OnDemand customers can subscribe to your Connect add-on. After a 30-day trial period, the customer
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
When you are ready to make your privately listed add-on public, initiate the process as follows:

1. Log in to the Atlassian Marketplace using a user account for the vendor associated with the add-on.
2. Click the **Manage Add-ons** link. 
3. Change the **Add-on Visibility** option to **Public**. 
4. Double check all your listing settings. Many settings become important in the context of a public
listing which are not for a private listing, such as the pricing model and marketing labels.
Marketing labels classify your add-on, and help customers find it on the Marketplace.
5. When ready, click the **Submit for approval** button at the bottom of the form.

This creates a JIRA issue that notifies the Marketplace team that your add-on is ready for review.
The Atlassian Marketplace team checks your add-on and the listing itself against the requirements
specified in the approval process. Note that the
[approval process](https://developer.atlassian.com/display/MARKET/Add-on+approval+guidelines#Add-onapprovalguidelines-CriteriaforAtlassianConnectadd-ons)
may take several days, and typically involves several rounds of feedback and updates to your listing.