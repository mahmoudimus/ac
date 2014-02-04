
# Release Notes

## [1.0-m30](../release-notes/1-0-m30.html)
* Support for [JIRA issue properties](../modules/jira/entity-property.html)
* Make [AP.messages](../javascript/module-messages.html) API stable
* Whitelisted remote endpoints are [listed in the documentation](../concepts/scopes.html)
* Fix bug with OAuth and JSON descriptor

Read the [1.0-m30 release notes](../release-notes/1-0-m30.html).

## [1.0-m29](../release-notes/1-0-m29.html)
* Tabs can now be added to Confluence Space Tools section. Check out [Space Tools Tab](../modules/confluence/space-tools-tab.html)
documentation for more information
* Support for [web sections](../modules/jira/web-section.html)
* Support for full screen dialogs
* AC Play support for JSON descriptor and JWT. Read the [upgrade guide](../guides/upgrade-play.html)

Read the [1.0-m29 release notes](../release-notes/1-0-m29.html).

## [1.0-m28](../release-notes/1-0-m28.html)
* New documentation for the Atlassian Connect Javascript API
* Java 7 is no longer required at runtime (change in atlassian-jwt 1.0-m8)
* JSON descriptors that request web-hooks must now also request the corresponding scopes required to receive these web-hooks
    * Without the correct scope you will see an error in the host product's log during installation that tells you which scope to add
* JIRA REST API endpoints are in the JSON descriptor scopes white-list
    * If your add-on uses these endpoints then you can now specify scopes in your descriptor and they will be respected in authorisation checks on requests to JIRA
    * E.g. add ```"scopes": ["READ", "WRITE"]``` to your JSON descriptor if your add-on performs read-only actions and mutating actions
    * Scopes white-list documentation coming soon so that you will be able to figure out which scope is required for each endpoint that you access

Read the [1.0-m28 release notes](../release-notes/1-0-m28.html).

## [1.0-m27](../release-notes/1-0-m27.html)

* Support for Macro image placeholder values
* Support for long query parameters for remote macros
* `web-item` module `link` attribute renamed to `url`
* Fixed bug which prevented incoming JWT requests from being accepted
* Fixed the configure page url with JSON descriptor
* Better error reporting and bug fixes for JSON descriptor
* Docs are now available _in product_. Just visit `https://HOSTNAME:PORT/CONTEXT_PATH/atlassian-connect/docs/`

Additionally, we have relaxed the deprecation period for the XML descriptor until __28th February, 2014__.

Read the [1.0-m27 release notes](../release-notes/1-0-m27.html).

## [1.0-m25](../release-notes/1-0-m25.html)
Atlassian Connect `1.0-m25` introduces a number of changes to how you will build add-ons for
Atlassian OnDemand. There are two important changes in this release: a new format for your add-on
descriptor and a new authentication method. Both of these changes are designed to help developers
build add-ons more quickly and easily.

* JSON Add-on Descriptor
* JSON Web Token (JWT) Authentication
* atlassian-connect-express `v0.9.0`

These new features replace the XML descriptor and OAuth, which are now deprecated. Please read the
[deprecation notices](../concepts/deprecations.html).

Read the [1.0-m25 release notes](../release-notes/1-0-m25.html).

## Earlier Releases
For earlier release notes, please see the [Atlassian Connect Blog](https://developer.atlassian.com/pages/viewrecentblogposts.action?key=AC).
