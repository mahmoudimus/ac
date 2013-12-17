# What's New

Atlassian Connect `1.0-m25` introduces a number of changes to how you will build add-ons for Atlassian OnDemand.

### New Documentation Portal

The Atlassian Connect dev team is pleased to preview our new documentation portal, which you're looking at right now! We
have spent considerable time responding to your feedback and have built these docs from the ground up to help you find
the information you're looking for.

### JSON Descriptor

The `atlassian-plugin.xml` descriptor is now _deprecated_ and should no longer be used. It has been replaced with
a more modern JSON descriptor.

<div class="aui-message warning">
    <p class="title">
        <span class="aui-icon icon-warning"></span>
        <strong>Important</strong>
    </p>
    `atlassian-plugin.xml` will be removed from Atlassian Connect on __10th February, 2014__. Any add-ons on the
    Atlassian Marketplace will need to be migrated before this date.
</div>

Further resources:

* [Migrating to the JSON descriptor](./migrating-from-xml-to-json-descriptor.html)
* [Upgrade your Atlassian Connect Express add-on](./upgrade-ace.html). version `0.9.0` supports the JSON descriptor out
of the box.
* Example add-ons
  * [webhook-inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) (todo)
  * [atlassian-connect-jira-example](https://bitbucket.org/atlassianlabs/atlassian-connect-jira-example) (todo)
  * [atlassian-connect-confluence-example](https://bitbucket.org/atlassianlabs/atlassian-connect-confluence-example) (todo)

Coming soon:

* Migration documentation for [AC Play](https://bitbucket.org/atlassian/atlassian-connect-play-java)
* How to migrate your Marketplace listing to JSON

### JWT

JSON Web Token (JWT) is a new authentication mechanism which is provided as an alternative to OAuth 1.0. We are
responding to significant feedback that OAuth 1.0 has been difficult to implement, debug and integrate into existing
solutions.

<div class="aui-message info">
    <p class="title">
        <span class="aui-icon icon-info"></span>
        <strong>Important</strong>
    </p>
    We recognise that replacing the authentication stack away from OAuth 1.0 may take some time. We'll be removing
    support for OAuth 1.0 in __May 2014__.
</div>

Read about JWT and how to implement it in our [authentication](../concepts/authentication.html) docs. We think JWT is
significantly more straight forward than OAuth 1.0!

[atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express/) version `0.9.0` comes with
support for JWT out of the box.

### atlassian-connect-express `v0.9.0`

We have released version `0.9.0` of [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express/),
with support for JWT and the JSON descriptor.

Version `0.9.0` no longer supports the XML descriptor or OAuth 1.0. Read how to
[upgrade your Atlassian Connect Express add-on](./upgrade-ace.html).