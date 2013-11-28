A Search Request View allows you to render a custom representation of a search result. Rendering a custom XML format
is a common example.

After an add-on declaring a Search Request View capability is installed, a new entry will show up in the
*Export* menu on the Issue Navigator page. Clicking the entry will redirect to the URL that is provided
by your add-on, passing in the issue keys, pagination information and the OAuth parameters that allow you
to verify the validity of the request.

To declare a Search Request View, you must mainly provide the URL that will handle the HTTP GET request.
This URL is relative to the base url of the descriptor. Here is an example:

    "jiraSearchRequestViews": [
       {
          "conditions": [
             {
                "condition": "user_is_logged_in",
                "invert": false
             }
          ],
          "description": {
             "i18n": "my.searchRequestView.desc",
             "value": "My description"
          },
          "name": {
             "i18n": "my.searchRequestView.name",
             "value": "My Name"
          },
          "url": "/search-request.csv",
          "weight": 10
       }
    ]

Your service will be invoked with these parameters:

* __issues__: A comma-separated list of issue keys
* __link__: A link back to the JIRA Issue Navigator where the action was invoked
* __startIssue__: The index of the first passed issue key in the list of all issues
* __endIssue__: The index of the last passed issue key in the list of all issues
* __totalIssues__: The number of issues in the entire search result
