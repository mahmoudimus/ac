# Modules

Modules are UI extension points that add-ons can use to insert content into various areas of the host application's interface. You implement a page module (along with others type of module you can use with Atlassian Connect, like webhooks) by declaring it in the [add-on descriptor](addon-descirptor.html) and implementing the add-on code that composes it.

Each application has module types that are specific for it, but there are some common types as well. For instance, both JIRA and Confluence support the `generalPages` and `profilePages` module, but only JIRA has `issueTabPages`.

The page module takes care of integrating the add-on content into the application for you. The add-on content automatically gets the page styles and decorators from the host application.

## Available pages
To discover page modules available for each Atlassian application, refer to the following application-specific module documentation

### Jira
 * [General Page]()
 * [Admin Page]()
 * [Profile Page]()
 * [Configure Page]()
 * []

### Confluence

