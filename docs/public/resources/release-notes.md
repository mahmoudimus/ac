
# Release Notes

## [1.0-m27](../release-notes/1-0-m27.html)

* Support for Macro image placeholder values
* Support for long query parameters for remote macros
* `web-item` module `link` attribute renamed to `url`
* Fixed bug which prevented incoming JWT requests from being accepted
* Fixed the configure page url with JSON descriptor
* Better error reporting and bug fixes for JSON descriptor
* Docs are now available _in product._ Just visit `https://HOSTNAME:PORT/CONTEXT_PATH/atlassian-connect/docs/`

Additionally, we have relaxed the deprecation period for the XML descriptor until __28th February, 2014__.

Read the [1.0-m27 release notes](../release-notes/1-0-m27.html).

## [1.0-m25](../release-notes/1-0-m25.html)
Atlassian Connect `1.0-m25` introduces a number of changes to how you will build add-ons for Atlassian OnDemand. There
are two important changes in this release: a new format for your add-on descriptor and a new authentication method. Both
of these changes are designed to help developers build add-ons more quickly and easily.

* JSON Add-on Descriptor
* JSON Web Token (JWT) Authentication
* atlassian-connect-express `v0.9.0`

These new features replace the XML descriptor and OAuth, which are now deprecated. Please read the
[deprecation notices](../concepts/deprecations.html).

Read the [1.0-m25 release notes](../release-notes/1-0-m25.html).

## Earlier Releases
For earlier release notes, please see the [Atlassian Connect Blog](https://developer.atlassian.com/pages/viewrecentblogposts.action?key=AC).
