
# What's New

## 1.0-m25
Atlassian Connect `1.0-m25` introduces a number of changes to how you will build add-ons for Atlassian OnDemand. There
are two important changes in this release: a new format for your add-on descriptor and a new authentication method. Both
of these changes are designed to help developers build add-ons more quickly and easily.

#### JSON Add-on Descriptor

With Atlassian Connect `1.0-m25`, you can write your add-on descriptor using a new JSON format. That format is extensively
documented in this guide.

We have written several guides to help you transition an `atlassian-plugin.xml` to the new descriptor format:

* [Migrating to the JSON descriptor](./migrating-from-xml-to-json-descriptor.html)
* [Upgrade your Atlassian Connect Express add-on](./upgrade-ace.html). Version `0.9.0` supports the JSON descriptor out
of the box.

We have also updated several of the example add-ons:

* [webhook-inspector](https://bitbucket.org/atlassianlabs/webhook-inspector)
* [atlassian-connect-jira-example](https://bitbucket.org/atlassianlabs/atlassian-connect-jira-example)
* [atlassian-connect-confluence-example](https://bitbucket.org/atlassianlabs/atlassian-connect-confluence-example)

More docs are coming soon:

* Migration documentation for [AC Play](https://bitbucket.org/atlassian/atlassian-connect-play-java)
* How to migrate your Marketplace listing to JSON

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    The `atlassian-plugin.xml` descriptor is now _deprecated_ and should no longer be used. `atlassian-plugin.xml` will be
    removed from Atlassian Connect on __10th February, 2014__. Any add-ons on the Atlassian Marketplace will need to be
    migrated before this date.
</div>

#### JSON Web Token (JWT) Authentication

JSON Web Token (JWT) is a new authentication mechanism which is provided as an alternative to OAuth 1.0. We are
responding to significant feedback that OAuth 1.0 is difficult to implement, debug and integrate into existing
applications.

Read about JWT and how to implement it in our [authentication](../concepts/authentication.html) docs. We think JWT is
significantly more straight forward to understand and implement than OAuth 1.0!

[atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express/) version `0.9.0` comes with
support for JWT out of the box.

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    OAuth authentication for Connect is now _deprecated_ and should no longer be used. However, we recognise that replacing
    the authentication stack away from OAuth 1.0 may take more time. We'll be removing support for OAuth 1.0 in __May 2014__.
</div>

#### atlassian-connect-express `v0.9.0`

We have released version `0.9.0` of [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express/),
with support for JWT and the JSON descriptor.

Version `0.9.0` no longer supports the XML descriptor or OAuth 1.0. Read how to
[upgrade your Atlassian Connect Express add-on](./upgrade-ace.html).

#### New Documentation

Lastly, the Atlassian Connect dev team is pleased to preview our new documentation portal, which you're looking at right now!
This documentation reflects all of the changes outlined here (JWT and JSON Descriptor). It completely documents all of the
add-on module types, and is generated directly from source code. We hope this will also make developing Connect add-ons
quicker and easier.

#### Known Limitations

* Permission scopes are not implemented in the JSON descriptor. We've temporarily allowed JSON descriptor add-ons to bypass all permissions when running in development mode
* Web items with a target of 'dialog' require an absolute url and the requests are not signed. This will be fixed early in the new year
* JSON descriptor add-ons aren't yet available to be deployed to OnDemand (we're targeting mid January 2014)
* The URL variable substitution format will change from `${variable}` to the standard URL template format `{variable}`
  in one of the next releases

## Earlier Releases
For earlier release notes, please see the [Atlassian Connect Blog](https://developer.atlassian.com/pages/viewrecentblogposts.action?key=AC).
