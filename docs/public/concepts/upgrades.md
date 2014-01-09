# Upgrading and Versioning your Add-on

As explained in [Private Listings](../guides/private-listings.html), you must list your add-on on the [Atlassian
Marketplace](https://marketplace.atlassian.com) (either publicly or privately) in order to install it on a production
Atlassian OnDemand instance. You may list __private__ add-ons without triggering any kind of approval process from the
Marketplace, but any __public__ add-on must go through an [initial approval](../guides/selling-on-marketplace.html) by
the Atlassian Marketplace team.

Once your add-on is listed and approved (if necessary), your add-on may be installed on OnDemand instances.

## Upgrading your add-on

At that point, your add-on can be changed independently at any time. Almost all changes you make to your add-on will be
to code inside your add-on's web app. For example, tweaking the look of a web panel, adding a configuration option or
catching a previously unhandled exception can all be done by writing and deploying new code to your servers. Users will
see these changes as soon as you update your web app. In many cases (e.g. catching an exception or adding a
configuration option) there is no immediate reason why end users should be aware that you made the change.

You may version these kinds of changes your add-on in any way you want, including not at all. Some services version with
dates, others with commit hashes, and others use named versioned.

Regardless of how you version and release your add-on itself, the Marketplace must keep up with the version of your
[descriptor file](../modules/). Whenever you change your add-on descriptor file ({{atlassian-connect.json}}), that file
must be updated on the Marketplace and installed on all client instances before that change will take effect.

## Changing your add-on's descriptor file

In the near future, the Marketplace will periodically poll your add-on's descriptor, notice when it changes,
automatically bump the version, and automatically update it in all installations within 24 hours.  For now, however, you
must manually upload a new version of your descriptor to the Marketplace when you have a change. This change will still
get pushed to all clients within 24 hours. Not every client will be updated simultaneously, so your add-on may need to
handle the old and new version of your descriptor simultaneously during the upgrade window.

In most cases updating these clients should be automatic and fast. However, there are a few circumstances where
updating will not be automatic:

1. You change your add-on listing from private to public. In this case, your change will trigger a manual Marketplace
approval, which usually takes 3-5 business days.

2. You change your add-on listing from free to paid. In this case, your change will also trigger a Marketplace approval,
but in addition any existing clients will have to approve the change to start paying for your add-on. They must choose
either to approve or to uninstall the add-on.

3. You change your add-on to require additional [scopes](./scopes.html) (a "scope" is a group of permissions). In this
case, the marketplace update will happen automatically, but your clients must be given the opportunity to approve the
new scopes. They must choose either to approve or to uninstall the add-on.

In case two and three, until an admin in each installation accepts the new scopes or the new pricing the old descriptor
will remain in effect. Your add-on will have to handle both old and new behavior. You have only one add-on web app but
many installations; for a period of time some installations will be using the old descriptor and some the new
descriptor.

Fortunately, there is a simple trick that will enable you to gracefully handle this transition: version your URLs.
Every request from the host product or the user's browser to your add-on hits a URL that you define in your add-on
descriptor. Let's imagine the scenario in which your original descriptor requests read and write scopes, but not delete,
and all of its URLs contain the string "v1". Later, you want to add a feature that deletes some data, and this will
require the delete scope. When you update your descriptor to also request the delete scope you change "v1" to "v2" in all
affected URLs. Now when your add-on receives a "v1" request you know that the users and host product do not allow
deleting, and when your add-on receives a "v2" request then deleting is possible.

