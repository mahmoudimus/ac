
# Release Notes

<div class="aui-message info">
	    <p class="title">
	        <span class="aui-icon icon-success"></span>
	        <strong>Latest Atlassian Connect versions</strong>
	    </p>
	    <table>
	        <tr>
	            <td>In production:</td>
	            <td><span id="connect-version-prd"/></td>
	        </tr>
	        <tr>
                <td>In development:</td>
                <td><span id="connect-version-dev"/></td>
            </tr>
		</table>
</div>

The version numbers for the Atlassian Connect framework and Atlassian products which support it are published in 
[this JSON file](https://developer.atlassian.com/static/connect/commands/connect-versions.json), which you can poll periodically to
find out about new production and development (bleeding edge) releases. You can start a local instance of JIRA 
or Confluence Cloud with Atlassian Connect as follows:

<a data-replace-text="If you are using a local installation [-]" class="aui-expander-trigger" aria-controls="runproduct-local">If you are using a local installation [+]</a>
<div id="runproduct-local" class="aui-expander-content">
    <span data-include="../assets/includes/runproduct-local.html">Loading...</span>
</div>

<a data-replace-text="If you are using the Vagrant box [-]" class="aui-expander-trigger" aria-controls="runproduct-vagrant">If you are using the Vagrant box [+]</a>
<div id="runproduct-vagrant" class="aui-expander-content">
    <span data-include="../assets/includes/runproduct-vagrant.html">Loading...</span>
</div>

## [1.1.55](../release-notes/1-1-0.html#1.1.55)
* Added `post-install-page` module for add-ons to provide a page with information on getting started [AC-1579](https://ecosystem.atlassian.net/browse/AC-1579)
* Confluence: Added fields to the `confluenceContentProperties` module to allow add-ons to provide aliases for content properties [CE-155](https://ecosystem.atlassian.net/browse/CE-155)
* Confluence: Added support for content property aliases in the UI of the CQL builder [CE-156](https://ecosystem.atlassian.net/browse/CE-156)
* REST endpoint `/secure/viewavatar` accessible with READ scope [AC-1748](https://ecosystem.atlassian.net/browse/AC-1748)
* JIRA: Added field to `data-options` which hides the footer on an add-on provided page [ACJIRA-220](https://ecosystem.atlassian.net/browse/ACJIRA-220)

## [1.1.52](../release-notes/1-1-0.html#1.1.52)
* Reduce required scope for JIRA project properties to WRITE [AC-1686](https://ecosystem.atlassian.net/browse/AC-1686)

## [1.1.48](../release-notes/1-1-0.html#1.1.48)
* JIRA: Whitelist REST API endpoint for unassigning epic from issues [ACJIRA-542](https://ecosystem.atlassian.net/browse/ACJIRA-542)

## [1.1.47](../release-notes/1-1-0.html#1.1.47)
* Remove OAuth 1.0 as an authentication type in the descriptor [AC-1688](https://ecosystem.atlassian.net/browse/AC-1688)

## [1.1.42](../release-notes/1-1-0.html#1.1.42)
* JIRA: Whitelist REST API for JIRA Agile [ACJIRA-491](https://ecosystem.atlassian.net/browse/ACJIRA-491)

## [1.1.41](../release-notes/1-1-0.html#1.1.41)
* JIRA: Fix issue and project permission conditions [AC-1666](https://ecosystem.atlassian.net/browse/AC-1666)

## [1.1.37](../release-notes/1-1-0.html#1.1.37)
* JIRA: Added ability to open Create Issue Dialog from the addon [ACJIRA-113](https://ecosystem.atlassian.net/browse/ACJIRA-113)
* JIRA: Added API to check if user is allowed to modify the dashboard [ACJIRA-436](https://ecosystem.atlassian.net/browse/ACJIRA-436)
* Fix non-responsive dialog chrome buttons when errors from loading content [ACJS-44](https://ecosystem.atlassian.net/browse/ACJS-44)
* Fixed javascript errors when pressing escape on a non-dialog iframe [ACJS-46](https://ecosystem.atlassian.net/browse/ACJS-46)
* Confluence: Added scope to create space with an addon [CE-157](https://ecosystem.atlassian.net/browse/CE-157)
* JIRA: Fixed refreshIssuePage to run on first call [AC-1599](https://ecosystem.atlassian.net/browse/AC-1599)

## [1.1.35](../release-notes/1-1-0.html#1.1.35)
* Fixed broken dialog button callbacks [ACJS-41](https://ecosystem.atlassian.net/browse/ACJS-41)

## [1.1.33](../release-notes/1-1-0.html#1.1.33)
* Confluence: Fixed broken generation of macroHash [CE-163](https://ecosystem.atlassian.net/browse/CE-163)

## [1.1.32](../release-notes/1-1-0.html#1.1.32)
* Add `entity_property_equal_to` condition for add-on properties [ACJIRA-362](https://ecosystem.atlassian.net/browse/ACJIRA-362)
* JIRA: Add `entity_property_equal_to` condition for project, issue, issue type and comment properties [ACJIRA-362](https://ecosystem.atlassian.net/browse/ACJIRA-362)
* JIRA: Provide dashboard items [ACJIRA-248](https://ecosystem.atlassian.net/browse/ACJIRA-248)
* Confluence: Add view mode parameter for blueprints [CE-110](https://ecosystem.atlassian.net/browse/CE-110)
* JIRA: Add issue type ID context parameter for issue tab panels [ACJIRA-417](https://ecosystem.atlassian.net/browse/ACJIRA-417)
* Confluence: Add macro.id context parameter for macros [CE-79](https://ecosystem.atlassian.net/browse/CE-79)
* JIRA: Whitelist REST API endpoints for dashboard items [ACJIRA-247](https://ecosystem.atlassian.net/browse/ACJIRA-247)
* JIRA: Whitelist REST API endpoints for project roles [ACJIRA-394](https://ecosystem.atlassian.net/browse/ACJIRA-394)
* Client-side JWT refresh fails for users with UTF-8 characters in the display name [ACJS-36](https://ecosystem.atlassian.net/browse/ACJS-36)
* Dialog close button doesn't work if there is an error loading the contents [ACJS-6](https://ecosystem.atlassian.net/browse/ACJS-6)
* Hotkeys cannot be used in dialogs until focused by the user [ACJS-22](https://ecosystem.atlassian.net/browse/ACJS-22)
* Confluence: : = | RAW | = : parameter causes errors in macros [AC-1573](https://ecosystem.atlassian.net/browse/AC-1573)
* JIRA: Admin pages lose navigation context [ACJIRA-114](https://ecosystem.atlassian.net/browse/ACJIRA-114)
* Errors in the JavaScript API are swallowed [ACJS-2](https://ecosystem.atlassian.net/browse/ACJS-2)

## [1.1.29](../release-notes/1-1-0.html#1.1.29)
* Username and display name available on the JWT token [AC-1558](https://ecosystem.atlassian.net/browse/AC-1558)
* Autoconvert: Limiter on number of possible patterns for a single macro

## [1.1.27](../release-notes/1-1-0.html#1.1.27)
* Confluence: Allow index schema configuration for content properties [CE-77](https://ecosystem.atlassian.net/browse/CE-77)
* Confluence: Support for extending autoconvert for macros [CE-33](https://ecosystem.atlassian.net/browse/CE-33)
* JIRA: Whitelist REST API methods for JQL auto-complete suggestions [ACJIRA-367](https://ecosystem.atlassian.net/browse/ACJIRA-367)
* Improve feedback for failed add-on installations through UPM [AC-1547](https://ecosystem.atlassian.net/browse/AC-1547)

## [1.1.25](../release-notes/1-1-0.html#1.1.25)
* Add REST API for storing and accessing add-on properties [ACJIRA-28](https://ecosystem.atlassian.net/browse/ACJIRA-28)
* JIRA: Whitelist REST API methods for comment properties [ACJIRA-306](https://ecosystem.atlassian.net/browse/ACJIRA-306)
* Confluence: confluence.closeMacroEditor() stopped working [AC-1525](https://ecosystem.atlassian.net/browse/AC-1525)

## [1.1.23](../release-notes/1-1-0.html#1.1.23)
 * JIRA: Fixed broken JavaScript API method - jira.refreshIssuePage() [ACDEV-1508](https://ecosystem.atlassian.net/browse/AC-1508)

## [1.1.22](../release-notes/1-1-0.html#1.1.22)
* Fixed error which prevented web-items with remote conditions from loading [AC-1503](https://ecosystem.atlassian.net/browse/AC-1503)
* Confluence: Fixed error which prevented retrieving a macro body by its hash [AC-1505](https://ecosystem.atlassian.net/browse/AC-1505)

## [1.1.21](../release-notes/1-1-0.html#1.1.21)
* Confluence: Added support for render modes for Dynamic Content Macros [CE-66](https://ecosystem.atlassian.net/browse/CE-66)
* JIRA: Added support for aliases in Entity Properties [ACJIRA-250](https://ecosystem.atlassian.net/browse/ACJIRA-250)
* JIRA: Added context parameters for Agile Boards [ACJIRA-272](https://ecosystem.atlassian.net/browse/ACJIRA-272)
* Improved REST API for license information [AC-1370](https://ecosystem.atlassian.net/browse/AC-1370)
* Confluence: Fixed error when inserting Connect macros in Internet Explorer [CE-74](https://ecosystem.atlassian.net/browse/CE-74)
* JIRA: Whitelisted endpoints for thumbnails and user avatars [AC-1472](https://ecosystem.atlassian.net/browse/AC-1472)
* JIRA: Fixed `has_issue_permission` or `has_project_permission` for web items [ACJIRA-263](https://ecosystem.atlassian.net/browse/ACJIRA-263)
* JIRA: Fixed rendering strategy for dialogs when JWT token expired [ACJIRA-275](https://ecosystem.atlassian.net/browse/ACJIRA-275)
* Enabled installation of add-ons without modules [AC-1439](https://ecosystem.atlassian.net/browse/AC-1439)

## [1.1.18](../release-notes/1-1-0.html#1.1.18)
* Fixed: Confluence macro editor replaces page content after it gets closed in Internet Explorer

## [1.1.17](../release-notes/1-1-0.html#1.1.17)
* Confluence: fixed a bug that made chromeless dialogs fail to display [AC-1449](https://ecosystem.atlassian.net/browse/AC-1449)
* JIRA: Expose system properties with REST API: [ACJIRA-123](https://ecosystem.atlassian.net/browse/ACJIRA-123)
* JIRA: Whitelist JIRA Agile REST calls for getting epics and adding issues to epics: [ACJIRA-219](https://ecosystem.atlassian.net/browse/ACJIRA-219)

## [1.1.15](../release-notes/1-1-0.html#1.1.15)
* Improvements in lifecycle webhook signatures.
* Fixed: SSL issues for addons hosted on OpenShift.

## [1.1.10](../release-notes/1-1-0.html#1.1.10)
* Fixed: add-on loaded twice in the Issue Navigator
* Fixed: adminpage iframe has padding / margin
* Send Connect version information to the add-on
* Scopes white-list JIRA user picker API

## [1.1.9](../release-notes/1-1-0.html#1.1.9)
* First phase for [Blueprints](../modules/confluence/blueprint.html)

## [1.1.8](../release-notes/1-1-0.html#1.1.8)
* Fixed: duplicate web panels in JIRA search-for-issues results
* Fixed: iFrame resizing in Chrome
* Better error message when group permissions prevent add-on installation

## [1.1.7](../release-notes/1-1-0.html#1.1.7)
* Bug fixes and minor improvements

## [1.1.6](../release-notes/1-1-0.html#1.1.6)
* More [JIRA reports](../modules/jira/report.html) features.
* Add-ons are no longer automatically uninstalled when the installation lifecycle hook returns an error response.

## [1.1.4](../release-notes/1-1-0.html#1.1.4)
* Confluence now supports `content.*` variables everywhere that `page.*` variables were supported.
* Support for [JIRA reports](../modules/jira/report.html)
* Dialogs and inline dialogs will no longer suffer from expired JWTs.
* Add-ons will no longer change from disabled to enabled as a result of automatic updates.

## [1.1.0-rc.4](../release-notes/1-1-0.html#rc4)
* Bug fixes and stability improvements

## [1.1.0-rc.3](../release-notes/1-1-0.html#rc3)
* __Read about [breaking changes](../release-notes/1-1-0.html#breaking-changes)__
* Fixed OAuth authenticated requests
* Temporarily removed support for Confluence Mobile

## [1.1.0-rc.2](../release-notes/1-1-0.html#rc2)
* __Read about [breaking changes](../release-notes/1-1-0.html#breaking-changes)__
* Whitelist Confluence Questions context params for webitems and webpanels

## [1.1.0-rc.1](../release-notes/1-1-0.html#rc1)
* __Read about [breaking changes](../release-notes/1-1-0.html#breaking-changes)__
* JavaScript API: removed deprecated function `AP.fireEvent()`
* New feature: condition for checking dark features
* Fixed: web-sections which rely on add-on-provided web-items fail to register
* Valid module keys are no longer modified
* Introduced Confluence Questions `content.id` / `content.version` / `content.type` / `content.plugin` [context parameters](../concepts/context-parameters.html) for webitems and webpanels
* Docs: Fix broken links to webhooks module page
* Docs: web panels are not to be used for dialog content

## [1.1.0-beta.5](../release-notes/1-1-0.html)
* __Read about [breaking changes](../release-notes/1-1-0.html#breaking-changes)__
* XML descriptor servlet paths redirect to new JSON descriptor paths
* Manage cookies through the [javascript cookie api](../javascript/module-cookie.html)
* Manage browser history through the [javascript history api](../javascript/module-history.html)
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

## [1.0.2](../release-notes/1-0-2.html)
* Fixed workflow post functions
* Support for hidden macros in the macro browser
* Removing content from Web-panel no longer leaves grey bar in place

## [1.0.1](../release-notes/1-0-1.html)
* Allow POST method for "screens/addToDefault/{fieldId}"
* UPM auto-update fails to upgrade add-on

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
* Support for [web sections](../modules/common/web-section.html)
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
[deprecation notices](../resources/deprecations.html).

Read the [1.0-m25 release notes](../release-notes/1-0-m25.html).

## Earlier Releases
For earlier release notes, please see the [Atlassian Connect Blog](https://developer.atlassian.com/pages/viewrecentblogposts.action?key=AC).