# Product API Browser

Atlassian REST API Browser is a tool for discovering the REST APIs available in Atlassian applications, including JIRA and Confluence.

The REST API Browser shows you the resources in the application, displays the methods for each resource, and allows you to make test calls against the methods.
The REST API Browser shows you the core application resources, as well as any exposed by plugins you have installed as well.
If the REST APIs use the prescribed Javadoc annotations, you will also see inline documentation, including parameter descriptions.

### In your product
If you are developing a Connect add-on and have [launched your host product](../developing/developing-locally.html) via the Atlassian SDK, you can find the REST API browser in your running product:

* `https://HOSTNAME:PORT/CONTEXT_PATH/plugins/servlet/restbrowser#/`

### Browsable online
You can explore our APIs even if you're not running a local copy of our products. We host the REST API Browser on a public service for each JIRA and Confluence:

* __JIRA__: [https://bunjil.jira-dev.com/plugins/servlet/restbrowser#/](https://bunjil.jira-dev.com/plugins/servlet/restbrowser#/)
* __Confluence__: [https://bunjil.jira-dev.com/wiki/plugins/servlet/restbrowser#/](https://bunjil.jira-dev.com/wiki/plugins/servlet/restbrowser#/)
