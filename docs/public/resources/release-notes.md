
# Release Notes

## [1.1.0-beta.4](../release-notes/1-1-0.html)
* __If you read one thing, read about [breaking changes](../release-notes/1-1-0.html#breaking-changes)__
* XML descriptor servlet paths redirect to new JSON descriptor paths
* Manage cookies through the [javascript cookie api](http://localhost:9000/javascript/module-cookie.html)
* Manage browser history through the [javascript history api](http://localhost:9000/javascript/module-history.html)
* Improve modal dialog, to introduce the chrome flag
* Dialog height is now always the height of the iframe
* Refresh a JIRA issue without reloading the page
* Fixed bug that allowed content to appear in wrong iframe
* Big speed improvements on addon install, enable, disable, uninstall etc.
* Additional REST paths added to scopes and whitelists
* Add-on keys and add-on module keys have stricter restrictions
* Fixed numerous workflow post function bugs
* Improved documentation for workflow post functions
* Remote web panels fixed in JIRA Agile

## [1.0.1](../release-notes/1-0-1.html)
* Allow POST method for "screens/addToDefault/{fieldId}"
* UPM auto-update fails to upgrade add-on

## [1.0.2](../release-notes/1-0-2.html)
* Fixed workflow post functions
* Support for hidden macros in the macro browser
* Removing content from Web-panel no longer leaves grey bar in place

## [1.0.0](../release-notes/1-0.html)
* Installing an add-on into OnDemand will not work unless the base url starts with https
* Support for context parameters on remote conditions
* The add-on key must now be less than or equal to 80 characters. Any add-ons with larger keys will need to be shortened
* Module `key` attributes are now required
* WebPanel url and location fields are now required
* Only add-ons with a [`baseUrl`](../modules#baseUrl) starting with ``https://`` can be installed in OnDemand servers. ``http://`` may still be used for testing locally.
* Increased [security](../concepts/security.html): add-ons are assigned a user in each product instance in which they are installed and server-to-server requests go through authorisation checks as this user.
* Fixes issue where `user_is_logged_in` condition caused general pages to not be viewable
* Fixes numerous issues with context parameters not being sent through to conditions and pages
* Removes page header from Confluence general pages

## [1.0-m31](../release-notes/1-0-m31.html)
* Support for Inline Dialogs
* The [`authentication`](../modules/authentication.html) module is now required
* Add-ons that request JWT authentication will now fail to install if they do not specify an ``"installed"``
[lifecycle callback](../modules/lifecycle.html). To opt out of JWT authentication, you may specify an authentication
type of ``"none"``.

## [1.0-m30](../release-notes/1-0-m30.html)
* Removal of email sending resource
* Support for [JIRA issue properties](../modules/jira/entity-property.html)
* Make [AP.messages](../javascript/module-messages.html) API stable
* Whitelisted remote endpoints are [listed in the documentation](../scopes/scopes.html)
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
