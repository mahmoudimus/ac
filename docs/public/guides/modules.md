# Modules

Modules are UI extension points that add-ons can use to insert content into various areas of the host application's interface. You implement a page module (along with others type of module you can use with Atlassian Connect, like webhooks) by declaring it in the [add-on descriptor](addon-descirptor.html) and implementing the add-on code that composes it.

Each application has module types that are specific for it, but there are some common types as well. For instance, both JIRA and Confluence support the `generalPages` module, but only Confluence has `profilePage`.

The page module takes care of integrating the add-on content into the application for you. The add-on content automatically gets the page styles and decorators from the host application.
<!-- ## Available pages
To discover page modules available for each Atlassian application, refer to the following application-specific module documentation -->

<!-- ### Jira
 * [General Page](../modules/jira/generalPages.html)
 * [Admin Page](../modules/jira/adminPages.html)
 * [Configure Page](../modules/jira/configurePages.html)

### Confluence
 * [General Page](../modules/confluence/generalPages.html)
 * [Admin Page](../modules/confluence/adminPages.html)
 * [Configure Page](../modules/confluence/configurePages.html)
 * [Profile Page](../modules/confluence/profilePges.html)
-->