# Product API Browser

Atlassian products have a rich set of REST APIs for you to use. These APIs are the way that you interact with the Atlassian application's features and data. The REST API Browser will help you get acquainted with the available APIs.

The REST API Browser shows you the resources in the application, displays the methods for each resource, and allows you to make test calls against the methods. The RAB shows you the core application resources, as well as any exposed by plugins you have installed as well. If the REST APIs use the prescribed Javadoc annotations, you will also see inline documentation, including parameter descriptions.

### In your product
If you are developing a Connect add-on and have [launched your host product](../developing/developing-locally.html) via the Atlassian SDK, you can find the REST API browser in your running product:

* `https://HOSTNAME:PORT/CONTEXT_PATH/plugins/servlet/restbrowser#/`


### Browsable online
Atlassian also hosts the REST API Browser on two of our public services, so you can explore the API even if you are not running your own copy of the products. Check out the REST API Browser on these Atlassian instances:

* JIRA: [https://bunjil.jira-dev.com/plugins/servlet/restbrowser#/](https://bunjil.jira-dev.com/plugins/servlet/restbrowser#/)
* Confluence: [https://bunjil.jira-dev.com/wiki/plugins/servlet/restbrowser#/](https://bunjil.jira-dev.com/wiki/plugins/servlet/restbrowser#/)
