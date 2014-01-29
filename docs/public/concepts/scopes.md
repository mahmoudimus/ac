# Scopes

Scopes have yet to be fully implemented. Please check back later.

* As of Connect version 1.0-m29 JIRA REST API endpoints are in the JSON descriptor scopes white-list.
    * If your add-on uses these endpoints then you can now specify scopes in your descriptor and they will be respected in authorisation checks on requests to JIRA.
    * E.g. add ```"scopes": ["READ", "WRITE"]``` to your JSON descriptor if your add-on performs read-only actions and mutating actions.
    * Scopes white-list documentation coming soon so that you will be able to figure out which scope is required for each endpoint that you access.
