# Add-on descriptor JSON schemas

The descriptor format for add-ons to each product is defined by a [JSON schema](http://json-schema.org).

* [Confluence](../schema/confluence-global-schema.json)
* [JIRA](../schema/jira-global-schema.json)

## Validator tool

Atlassian Connect provides a stand-alone validation service for add-on descriptors.

* https://atlassian-connect-validator.herokuapp.com/validate

The validator will check that your descriptor is syntactically correct. Just paste the JSON content
of your descriptor in the `descriptor` field, and select the Atlassian product you want to validate
against.
