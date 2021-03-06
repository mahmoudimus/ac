# Product API Browser

Atlassian REST API Browser is a tool for discovering the REST APIs available in Atlassian applications, including JIRA and Confluence.

The REST API Browser shows you the resources in the application, displays the methods for each resource, and allows you to make test calls against the methods.
The REST API Browser shows you the core application resources, as well as any exposed by plugins you have installed as well.
If the REST APIs use the prescribed Javadoc annotations, you will also see inline documentation, including parameter descriptions.

### In your product
If you are developing a Connect add-on and have [launched your host product](../developing/developing-locally.html) via the Atlassian SDK, you can find the REST API browser in your running product:

* `https://HOSTNAME:PORT/CONTEXT_PATH/plugins/servlet/restbrowser#/`
