Page modules are UI extension points that add-ons can use to insert content into various areas of the host
application's interface. You implement a page module (along with the other type of module you can use with
Atlassian Connect, webhooks) by declaring it in the add-on descriptor and implementing the add-on code that
composes it.

Each application has page module types that are specific for it, but there are some common page types as well.
For instance, both JIRA and Confluence support the general-page and profile-page module, but only JIRA has the
issue-panel-page.

The page module takes care of integrating the add-on content into the application for you. The add-on content
automatically gets the page styles and decorators from the host application.
