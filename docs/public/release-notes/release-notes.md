
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

## [1.1.78](./1-1-0.html#1.1.78)
 * New JavaScript close trigger for AP.messages [ACJS-12](https://ecosystem.atlassian.net/browse/ACJS-12)
 * JIRA: Fix for contextual `or` subconditions to be treated as true in context free environments [AC-1882](https://ecosystem.atlassian.net/browse/AC-1882)
 * JIRA: Fix to ensure that contextual information on jiraIssueTabPanels is wired correctly [AC-1883](https://ecosystem.atlassian.net/browse/AC-1883)
 * Confluence: Allow hidden parameters for macros [CE-300](https://ecosystem.atlassian.net/browse/CE-300)

## [1.1.77](./1-1-0.html#1.1.77)
 * New `has_global_permission` condition [ACJIRA-777](https://ecosystem.atlassian.net/browse/ACJIRA-777)
 * Nested equality comparisons for addon properties [AC-1847](https://ecosystem.atlassian.net/browse/AC-1847)
 * JIRA: Date picker component [ACJIRA-729](https://ecosystem.atlassian.net/browse/ACJIRA-729)


## [1.1.76](./1-1-0.html#1.1.76)
 * Navigator context API for unsaved edit page [CE-289](https://ecosystem.atlassian.net/browse/CE-289)
 * Navigator context API for contentedit [CE-278](https://ecosystem.atlassian.net/browse/CE-278)
 * Fixed Confluence storage format of macro content not found when using nested block macro [CE-220](https://ecosystem.atlassian.net/browse/CE-220)
 * Make requests to experimental APIs [ACJS-75](https://ecosystem.atlassian.net/browse/ACJS-75)

## [1.1.75](./1-1-0.html#1.1.75)
 * Added context parameters for conditions [ACJIRA-778](https://ecosystem.atlassian.net/browse/ACJIRA-778)
 * Confluence: Added ability to pass in dynamic variables to Connect Blueprints [CE-252](https://ecosystem.atlassian.net/browse/CE-252)
 * URLs for web panels in drop-downs can no longer become stale [ACJIRA-724](https://ecosystem.atlassian.net/browse/ACJIRA-724)
 * Fixed incorrect handling of query parameters in lifecycle handlers [AC-1825](https://ecosystem.atlassian.net/browse/AC-1825)

## [1.1.71](./1-1-0.html#1.1.71)
 * Confluence: Enable chromeless dialogs for custom macro editors [CE-283](https://ecosystem.atlassian.net/browse/CE-283)

## [1.1.69](./1-1-0.html#1.1.69)
 * JIRA: Whitelist worklog resource (see the [JIRA REST Scopes page](../scopes/jira-rest-scopes.html)) [AC-1826](https://ecosystem.atlassian.net/browse/AC-1826)

## [1.1.68](./1-1-0.html#1.1.68)
 * Add new `addon_is_licensed` condition [AC-1641](https://ecosystem.atlassian.net/browse/AC-1641)
 * Allow multiple dialogs to be open at the same time [ACJS-91](https://ecosystem.atlassian.net/browse/ACJS-91)
 * JIRA: Added in the `board.type` and `board.state` context parameters and deprecated the `board.mode` parameter in favour of `board.screen`. [ACJIRA-593](https://ecosystem.atlassian.net/browse/ACJIRA-593)

## [1.1.67](./1-1-0.html#1.1.67)
 * JIRA: Whitelist REST APIs for JIRA Service Desk [AC-1774](https://ecosystem.atlassian.net/browse/AC-1774)
 * JIRA: Whitelist REST APIs for JIRA Agile [ACJIRA-709](https://ecosystem.atlassian.net/browse/ACJIRA-709)
 * Allow to pass custom data to the add-on when opening a dialog [ACJS-10](https://ecosystem.atlassian.net/browse/ACJS-10)
 * Fullscreen Dialog with control bar at top [ACJS-83](https://ecosystem.atlassian.net/browse/ACJS-83)
 * Confluence: Add JavaScript API for browser navigation [CE-249](https://ecosystem.atlassian.net/browse/CE-249)
 * Add JavaScript API for refreshing the browser [CE-249](https://ecosystem.atlassian.net/browse/CE-249)
 * JIRA: Add scope whitelisting for user properties [AC-1809](https://ecosystem.atlassian.net/browse/AC-1809)
 * Web items with JWTs fail on stale pages [ACJIRA-294](https://ecosystem.atlassian.net/browse/ACJIRA-294)

## [1.1.65](./1-1-0.html#1.1.65)
 * Add-on Properties GET request can provide the non-escaped JSON value with a query parameter [AC-1693](https://ecosystem.atlassian.net/browse/AC-1693)
 * JIRA: Added condition to allow a query for available products [ACJIRA-590](https://ecosystem.atlassian.net/browse/ACJIRA-590)

## [1.1.64](./1-1-0.html#1.1.64)
 * JIRA: Two new modules for you to define global and project permissions for your JIRA add-ons.
 * JIRA Software: The 'agileBoard.id' and 'agileBoard.mode' content properties became the 'board.id' and 'board.mode' content properties respectively. And another JIRA Software content property has been added 'sprint.id'.

## [1.1.60](./1-1-0.html#1.1.60)
 * JIRA: Make jira.openCreateIssueDialog() available on general admin and project admin pages [AC-1652](https://ecosystem.atlassian.net/browse/AC-1652)
 * JIRA: Fixed issue when installing add-on with ADMIN scope, affecting some versions of upstream services [AC-1771](https://ecosystem.atlassian.net/browse/AC-1771)

## [1.1.57](./1-1-0.html#1.1.57)
* Fixed web item actions being broken by certain conditions [AC-1640](https://ecosystem.atlassian.net/browse/AC-1640)
* More specific error messages shown for SSL-related installation errors [AC-1216](https://ecosystem.atlassian.net/browse/AC-1216)

## [1.1.55](./1-1-0.html#1.1.55)
* Added `post-install-page` module for add-ons to provide a page with information on getting started [AC-1579](https://ecosystem.atlassian.net/browse/AC-1579)
* Confluence: Added fields to the `confluenceContentProperties` module to allow add-ons to provide aliases for content properties [CE-155](https://ecosystem.atlassian.net/browse/CE-155)
* Confluence: Added support for content property aliases in the UI of the CQL builder [CE-156](https://ecosystem.atlassian.net/browse/CE-156)
* REST endpoint `/secure/viewavatar` accessible with READ scope [AC-1748](https://ecosystem.atlassian.net/browse/AC-1748)
* JIRA: Added field to `data-options` which hides the footer on an add-on provided page [ACJIRA-220](https://ecosystem.atlassian.net/browse/ACJIRA-220)

## [1.1.52](./1-1-0.html#1.1.52)
* Reduce required scope for JIRA project properties to WRITE [AC-1686](https://ecosystem.atlassian.net/browse/AC-1686)

## [1.1.48](./1-1-0.html#1.1.48)
* JIRA: Whitelist REST API endpoint for unassigning epic from issues [ACJIRA-542](https://ecosystem.atlassian.net/browse/ACJIRA-542)

## [1.1.47](./1-1-0.html#1.1.47)
* Remove OAuth 1.0 as an authentication type in the descriptor [AC-1688](https://ecosystem.atlassian.net/browse/AC-1688)

## [1.1.42](./1-1-0.html#1.1.42)
* JIRA: Whitelist REST API for JIRA Agile [ACJIRA-491](https://ecosystem.atlassian.net/browse/ACJIRA-491)

## [1.1.41](./1-1-0.html#1.1.41)
* JIRA: Fix issue and project permission conditions [AC-1666](https://ecosystem.atlassian.net/browse/AC-1666)

## [1.1.37](./1-1-0.html#1.1.37)
* JIRA: Added ability to open Create Issue Dialog from the addon [ACJIRA-113](https://ecosystem.atlassian.net/browse/ACJIRA-113)
* JIRA: Added API to check if user is allowed to modify the dashboard [ACJIRA-436](https://ecosystem.atlassian.net/browse/ACJIRA-436)
* Fix non-responsive dialog chrome buttons when errors from loading content [ACJS-44](https://ecosystem.atlassian.net/browse/ACJS-44)
* Fixed javascript errors when pressing escape on a non-dialog iframe [ACJS-46](https://ecosystem.atlassian.net/browse/ACJS-46)
* Confluence: Added scope to create space with an addon [CE-157](https://ecosystem.atlassian.net/browse/CE-157)
* JIRA: Fixed refreshIssuePage to run on first call [AC-1599](https://ecosystem.atlassian.net/browse/AC-1599)

## [1.1.35](./1-1-0.html#1.1.35)
* Fixed broken dialog button callbacks [ACJS-41](https://ecosystem.atlassian.net/browse/ACJS-41)

## [1.1.33](./1-1-0.html#1.1.33)
* Confluence: Fixed broken generation of macroHash [CE-163](https://ecosystem.atlassian.net/browse/CE-163)

## [1.1.32](./1-1-0.html#1.1.32)
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

## [1.1.29](./1-1-0.html#1.1.29)
* Username and display name available on the JWT token [AC-1558](https://ecosystem.atlassian.net/browse/AC-1558)
* Autoconvert: Limiter on number of possible patterns for a single macro

## [1.1.27](./1-1-0.html#1.1.27)
* Confluence: Allow index schema configuration for content properties [CE-77](https://ecosystem.atlassian.net/browse/CE-77)
* Confluence: Support for extending autoconvert for macros [CE-33](https://ecosystem.atlassian.net/browse/CE-33)
* JIRA: Whitelist REST API methods for JQL auto-complete suggestions [ACJIRA-367](https://ecosystem.atlassian.net/browse/ACJIRA-367)
* Improve feedback for failed add-on installations through UPM [AC-1547](https://ecosystem.atlassian.net/browse/AC-1547)

## [1.1.25](./1-1-0.html#1.1.25)
* Add REST API for storing and accessing add-on properties [ACJIRA-28](https://ecosystem.atlassian.net/browse/ACJIRA-28)
* JIRA: Whitelist REST API methods for comment properties [ACJIRA-306](https://ecosystem.atlassian.net/browse/ACJIRA-306)
* Confluence: confluence.closeMacroEditor() stopped working [AC-1525](https://ecosystem.atlassian.net/browse/AC-1525)

## [1.1.23](./1-1-0.html#1.1.23)
 * JIRA: Fixed broken JavaScript API method - jira.refreshIssuePage() [ACDEV-1508](https://ecosystem.atlassian.net/browse/AC-1508)

## [1.1.22](./1-1-0.html#1.1.22)
* Fixed error which prevented web-items with remote conditions from loading [AC-1503](https://ecosystem.atlassian.net/browse/AC-1503)
* Confluence: Fixed error which prevented retrieving a macro body by its hash [AC-1505](https://ecosystem.atlassian.net/browse/AC-1505)

## [1.1.21](./1-1-0.html#1.1.21)
* Confluence: Added support for render modes for Dynamic Content Macros [CE-66](https://ecosystem.atlassian.net/browse/CE-66)
* JIRA: Added support for aliases in Entity Properties [ACJIRA-250](https://ecosystem.atlassian.net/browse/ACJIRA-250)
* JIRA: Added context parameters for Agile Boards [ACJIRA-272](https://ecosystem.atlassian.net/browse/ACJIRA-272)
* Improved REST API for license information [AC-1370](https://ecosystem.atlassian.net/browse/AC-1370)
* Confluence: Fixed error when inserting Connect macros in Internet Explorer [CE-74](https://ecosystem.atlassian.net/browse/CE-74)
* JIRA: Whitelisted endpoints for thumbnails and user avatars [AC-1472](https://ecosystem.atlassian.net/browse/AC-1472)
* JIRA: Fixed `has_issue_permission` or `has_project_permission` for web items [ACJIRA-263](https://ecosystem.atlassian.net/browse/ACJIRA-263)
* JIRA: Fixed rendering strategy for dialogs when JWT token expired [ACJIRA-275](https://ecosystem.atlassian.net/browse/ACJIRA-275)
* Enabled installation of add-ons without modules [AC-1439](https://ecosystem.atlassian.net/browse/AC-1439)

## [1.1.18](./1-1-0.html#1.1.18)
* Fixed: Confluence macro editor replaces page content after it gets closed in Internet Explorer

## [1.1.17](./1-1-0.html#1.1.17)
* Confluence: fixed a bug that made chromeless dialogs fail to display [AC-1449](https://ecosystem.atlassian.net/browse/AC-1449)
* JIRA: Expose system properties with REST API: [ACJIRA-123](https://ecosystem.atlassian.net/browse/ACJIRA-123)
* JIRA: Whitelist JIRA Agile REST calls for getting epics and adding issues to epics: [ACJIRA-219](https://ecosystem.atlassian.net/browse/ACJIRA-219)

## [1.1.15](./1-1-0.html#1.1.15)
* Improvements in lifecycle webhook signatures.
* Fixed: SSL issues for addons hosted on OpenShift.

## [1.1.10](./1-1-0.html#1.1.10)
* Fixed: add-on loaded twice in the Issue Navigator
* Fixed: adminpage iframe has padding / margin
* Send Connect version information to the add-on
* Scopes white-list JIRA user picker API

## [1.1.9](./1-1-0.html#1.1.9)
* First phase for [Blueprints](../modules/confluence/blueprint.html)

## [1.1.8](./1-1-0.html#1.1.8)
* Fixed: duplicate web panels in JIRA search-for-issues results
* Fixed: iFrame resizing in Chrome
* Better error message when group permissions prevent add-on installation

## [1.1.7](./1-1-0.html#1.1.7)
* Bug fixes and minor improvements

## [1.1.6](./1-1-0.html#1.1.6)
* More [JIRA reports](../modules/jira/report.html) features.
* Add-ons are no longer automatically uninstalled when the installation lifecycle hook returns an error response.

## [1.1.4](./1-1-0.html#1.1.4)
* Confluence now supports `content.*` variables everywhere that `page.*` variables were supported.
* Support for [JIRA reports](../modules/jira/report.html)
* Dialogs and inline dialogs will no longer suffer from expired JWTs.
* Add-ons will no longer change from disabled to enabled as a result of automatic updates.

## [1.1.0-rc.4](./1-1-0.html#rc4)
* Bug fixes and stability improvements

## [1.1.0-rc.3](./1-1-0.html#rc3)
* __Read about [breaking changes](./1-1-0.html#breaking-changes)__
* Fixed OAuth authenticated requests
* Temporarily removed support for Confluence Mobile

## [1.1.0-rc.2](./1-1-0.html#rc2)
* __Read about [breaking changes](./1-1-0.html#breaking-changes)__
* Whitelist Confluence Questions context params for webitems and webpanels

## [1.1.0-rc.1](./1-1-0.html#rc1)
* __Read about [breaking changes](./1-1-0.html#breaking-changes)__
* JavaScript API: removed deprecated function `AP.fireEvent()`
* New feature: condition for checking dark features
* Fixed: web-sections which rely on add-on-provided web-items fail to register
* Valid module keys are no longer modified
* Introduced Confluence Questions `content.id` / `content.version` / `content.type` / `content.plugin` [context parameters](../concepts/context-parameters.html) for webitems and webpanels
* Docs: Fix broken links to webhooks module page
* Docs: web panels are not to be used for dialog content

## [1.1.0-beta.5](./1-1-0.html#beta-5)
* __Read about [breaking changes](./1-1-0.html#breaking-changes)__
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

## [1.0.2](./1-0-2.html)
* Fixed workflow post functions
* Support for hidden macros in the macro browser
* Removing content from Web-panel no longer leaves grey bar in place

## [1.0.1](./1-0-1.html)
* Allow POST method for "screens/addToDefault/{fieldId}"
* UPM auto-update fails to upgrade add-on

## [1.0.0](./1-0.html)
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

## [1.0-m31](./1-0-m31.html)
* Support for Inline Dialogs
* The [`authentication`](../modules/authentication.html) module is now required
* Add-ons that request JWT authentication will now fail to install if they do not specify an ``"installed"``
[lifecycle callback](../modules/lifecycle.html). To opt out of JWT authentication, you may specify an authentication
type of ``"none"``.

## [1.0-m30](./1-0-m30.html)
* Removal of email sending resource
* Support for [JIRA issue properties](../modules/jira/entity-property.html)
* Make [AP.messages](../javascript/module-messages.html) API stable
* Whitelisted remote endpoints are [listed in the documentation](../scopes/scopes.html)
* Fix bug with OAuth and JSON descriptor

Read the [1.0-m30 release notes](./1-0-m30.html).

## [1.0-m29](./1-0-m29.html)
* Tabs can now be added to Confluence Space Tools section. Check out [Space Tools Tab](../modules/confluence/space-tools-tab.html)
documentation for more information
* Support for [web sections](../modules/common/web-section.html)
* Support for full screen dialogs
* AC Play support for JSON descriptor and JWT. Read the [upgrade guide](../guides/upgrade-play.html)

Read the [1.0-m29 release notes](./1-0-m29.html).

## [1.0-m28](./1-0-m28.html)
* New documentation for the Atlassian Connect Javascript API
* Java 7 is no longer required at runtime (change in atlassian-jwt 1.0-m8)
* JSON descriptors that request web-hooks must now also request the corresponding scopes required to receive these web-hooks
    * Without the correct scope you will see an error in the host product's log during installation that tells you which scope to add
* JIRA REST API endpoints are in the JSON descriptor scopes white-list
    * If your add-on uses these endpoints then you can now specify scopes in your descriptor and they will be respected in authorisation checks on requests to JIRA
    * E.g. add ```"scopes": ["READ", "WRITE"]``` to your JSON descriptor if your add-on performs read-only actions and mutating actions
    * Scopes white-list documentation coming soon so that you will be able to figure out which scope is required for each endpoint that you access

Read the [1.0-m28 release notes](./1-0-m28.html).

## [1.0-m27](./1-0-m27.html)

* Support for Macro image placeholder values
* Support for long query parameters for remote macros
* `web-item` module `link` attribute renamed to `url`
* Fixed bug which prevented incoming JWT requests from being accepted
* Fixed the configure page url with JSON descriptor
* Better error reporting and bug fixes for JSON descriptor
* Docs are now available _in product_. Just visit `https://HOSTNAME:PORT/CONTEXT_PATH/atlassian-connect/docs/`

Additionally, we have relaxed the deprecation period for the XML descriptor until __28th February, 2014__.

Read the [1.0-m27 release notes](./1-0-m27.html).

## [1.0-m25](./1-0-m25.html)
Atlassian Connect `1.0-m25` introduces a number of changes to how you will build add-ons for
Atlassian OnDemand. There are two important changes in this release: a new format for your add-on
descriptor and a new authentication method. Both of these changes are designed to help developers
build add-ons more quickly and easily.

* JSON Add-on Descriptor
* JSON Web Token (JWT) Authentication
* atlassian-connect-express `v0.9.0`

These new features replace the XML descriptor and OAuth, which are now deprecated. Please read the
[deprecation notices](../resources/deprecations.html).

Read the [1.0-m25 release notes](./1-0-m25.html).

## Earlier Releases
For earlier release notes, please see the [Atlassian Connect Blog](https://developer.atlassian.com/pages/viewrecentblogposts.action?key=AC).
