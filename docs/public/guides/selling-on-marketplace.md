# Selling on the Atlassian Marketplace

To sell your Atlassian Connect add-on on the [Atlassian Marketplace](https://marketplace.atlassian.com/) you need to create a public listing for it. A publicly listed add-on is available to any OnDemand customer, whether the add-on is free or paid via Atlassian.

Most add-ons are published to private listings on the Marketplace well before they are made public. Private listings allow developers to test the add-on and use it with their own OnDemand instances.

With the listing already in place then, to make the add-on public, you usually only need to switch the visibility setting for the listing and submit the add-on for approval from the Marketplace team. In practice, there's often more to it than that, however. Publicly available add-ons require suitable marketing assets (such as logos and screenshots), documentation, and support arrangements, among other things. As part of the add-on approval process, the Marketplace team makes sure that these requirements are met.

This page provides an overview of the process for making an add-on publicly available on the Marketplace. For information on creating the initial listing, see [Listing Private Add-ons](./private-listings.html).

## Background information
There are some general requirements and recommendations for selling on the Marketplace that apply to both Atlassian Connect add-ons and traditional downloadable add-ons. A few places to get started are:

- [Marketplace FAQ](https://www.atlassian.com/licensing/marketplace) contains customer-facing information on the Marketplace.
- [Atlassian Marketplace developer's site](https://www.atlassian.com/licensing/marketplace) contains general information about selling add-ons on the Marketplace. Some of that information is specific to downloadable, Java add-on.  
- [Add-on approval guidelines](https://developer.atlassian.com/display/MARKET/Add-on+approval+guidelines#Add-onapprovalguidelines-CriteriaforAtlassianConnectadd-ons), including some guidelines specific to Atlassian Connect.  

## How customers purchase Atlassian Connect add-ons
The OnDemand try and buy flow for Atlassian Connect add-ons is similar to that of traditional downloadable add-ons, but with a few differences. These include:

- Since add-on reside remotely from the OnDemand instances, installation of the add-on in OnDemand really means that only the plugin descriptor is installed in the OnDemand instance.
- For installable Atlassian applications, purchasing an add-on license means acquiring a license with an active maintenance period. In OnDemand, this is considered subscribing to the add-on. Essentially, subscribing to the add-on registers its descriptor and activates the license.
- For installable Atlassian applications, admins manage license codes for each add-on. In Atlassian OnDemand, admins simply configure their instance to include certain products and add-ons. Administrators never need to manage license codes.
- If the administrator cancels a trial or unsubscribes to the add-on, the add-on may remain in the UI until the add-on is uninstalled in UPM. For downloadable Atlassian application, uninstalling removes the JAR file from the instance. For OnDemand, this removes removes any elements of the add-on from the application's UI.
- Atlassian Connect add-on trials automatically roll into a subscription, and then roll from one month into another.

You should keep in mind that installation and licensing are separate functions for Atlassian Connect add-ons. Thus, an add-on with an expired license can retain a presence in the UI and retain data generated while active. Every request made to your add-on includes the license status for that particular instance. It is up to you to take appropriate action for a request where the license state is not valid.

For more about how administrators install and manage add-ons, see the [Universal Plugin Manager documentation](https://confluence.atlassian.com/display/UPM/About+the+Universal+Plugin+Manager).

## Creating Marketing Assets for your add-on
You can associate logos, screenshots and other marketing assets for your listing by referring to the assets bundle in the add-on descriptor.

For an Atlassian Connect add-on compatible with OnDemand, we suggest calling out OnDemand compatibility in your banner image or screenshots.

## Making your listing public
When you are ready to make your privately listed add-on public, initiate the process as follows:

1. Log in to the Atlassian Marketplace using a user account for the vendor associated with the add-on.
2. Click the **Manage Add-ons** link. 
3. Change the **Add-on Visibility** option to **Public**. 
4. Double check all your listing settings. Many settings become important in the context of a public listing which are not for a private listing, such as the pricing model and marketing labels. Marketing labels classify your add-on, and help customers find it on the Marketplace.  
5. When ready, click the **Submit for approval** button at the bottom of the form.

This creates a JIRA issue that notifies the Marketplace team that your add-on is ready for review. The Atlassian Marketplace team checks your add-on and the listing itself against the requirements specified in the approval process. Note that the [approval process](https://developer.atlassian.com/display/MARKET/Add-on+approval+guidelines#Add-onapprovalguidelines-CriteriaforAtlassianConnectadd-ons) may take several days, and typically involves several rounds of feedback and updates to your listing.