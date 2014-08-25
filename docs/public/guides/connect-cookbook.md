
# Connect cookbook: Common patterns

This section features code snippets you can use for common Connect add-on purposes, like accessing 
a JIRA project list or getting a list of pages from a Confluence space. You can use this section like a 
cookbook. 

## What's here
Skip ahead to any section

[Front-end patterns](#frontend): 

* [Loading `all.js` from host applications](#all.js)  
* [Accessing a list of JIRA projects](#jira-projects)   
* [Searching JIRA with JQL](#jql-search)   
* [Creating a JIRA issue](#jira-create-issue)  
* [Searching JIRA with JQL](#jql-search)  
* [Using a `messages` module](#messages-module)  
* [Flashing a warning in JIRA](#warning)  
* [Making messages disappear](#disappearing-meassages)  
* [Getting a list of Confluence spaces](#get-confluence-spaces)  
* [Getting information about specific spaces](#get-specific-space)  
* [Getting Confluence space pages](#getting-space-pages)  
* [Using the `cookie` API](#cookie-api)  



## <a name="frontend"></a> Front-end patterns

These patterns use the Connect [JavaScript APIs](../concepts/javascript-api.html) to perform basic functions in
Atlassian products. We use `console.log()` in place of actual business logic, so you can replace calls with whatever 
you need in your own add-on.  


### <a name="all.js"></a> Loading `all.js` from the host application

Loading `all.js` is necessary for you to use the [`AP` object](../javascript/module-AP.html) and access [Connect APIs](../concepts/javascript-api.html). 
This method requires jQuery (reference the script tag) to load `all.js` from the host application (like Confluence or 
JIRA) server. This sets up a cross-domain messaging bridge (XDM) so that you can access the content from the main product 
iframe. 


````
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script>
(function() {
  var getUrlParam = function (param) {
    var codedParam = (new RegExp(param + '=([^&]+)')).exec(window.location.search)[1];
    return decodeURIComponent(codedParam);
  };
  
  var baseUrl = getUrlParam('xdm_e') + getUrlParam('cp');
  $.getScript(baseUrl + '/atlassian-connect/all.js', function() {
    // your calls to AP here
    console.log("I can use AP now!");
  });
})();
</script>
````

### <a name="jira-projects"></a> Accessing list of JIRA projects

This snippet retrieves a list of JIRA projects. You might have to page through the list (as with most of these resources) 
by passing a `start` in the request query string. Looking at the results will help you identify the location of the result 
set so you know if you need to paginate further results.


````
AP.require('request', function(request) {
  request({
    url: '/rest/api/latest/project',
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="jql-search"></a> Search for an issue using JQL in JIRA

In this example you'll create a simple [JQL (JIRA query language)](https://confluence.atlassian.com/x/ghGyCg) 
query that looks for unresolved issues (`resolution = null`). This query is stored in the `searchJql` parameter.
You might need to paginate your results to get through all of them.


````
var searchJql = 'resolution = null';
AP.require('request', function(request) {
  request({
    url: '/rest/api/latest/search?jql=' + encodeURIComponent(searchJql),
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="jira-create-issue"></a> Creating JIRA issues

This recipe creates a new issue in JIRA. Project keys need to match, of course, and keep in mind your 
project might require additional fields depending on setup. You might also see our [JIRA REST examples](https://developer.atlassian.com/display/JIRADEV/JIRA+REST+API+Example+-+Create+Issue) 
for reference.


````
var issueData = {
  "fields": {
    "project": { 
      "key": "TEST"
    },
    "summary": "REST ye merry gentlemen.",
    "description": "Creating of an issue using project keys and issue type names using the REST API",
    "issuetype": {
      "name": "Task"
    }
  }
};
AP.require('request', function(request) {
  request({
    url: '/rest/api/latest/issue',
    // adjust to a POST instead of a GET
    type: 'POST',
    data: JSON.stringify(issueData),
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="messages-module"></a> Using a `messages` module

This recipe creates a simple message box on the page that users can dismiss. The two parameters of 
`message.info` can be adjusted for title and content. 


````
AP.require('messages', function(messages) {
  messages.info('Message box', 'This message box has some content. Hi!'); 
});
````

### <a name="warning"></a> Flashing a warning for 5 seconds with a 1-second fadeout

Use this snippet to create a hint-style message box that's visible for 5 seconds, and fades out for 
1 second.


````
AP.require('messages', function(messages) {
  messages.hint('Lookie here', 'I am fading away', { fadeout: true, delay: 5000, duration: 1000 });
});
````

### <a name="disappearing-messages"></a> Disappearing messages without fadeout effects

Rather than fading away, you can make your message simply disappear. This creates an error popup that doesn't 
disappear until other code requests for it to vanish. This code is currently called on a 5-second timer. 
Note that calling `messages.clear` with an already-cleared `messageID` doesn't have any effect.


````
AP.require('messages', function(messages) {
  var msgId = messages.error('Lookie here', 'I am only here for a few seconds');
  setTimeout(function() { messages.clear(msgId); }, 5000);
});
````

### Creating modules for `AP.require`

Placeholder - for Travis

### <a name="get-spaces"></a> Getting Confluence spaces

This retrieves a list of spaces from Confluence, and may require paging through the results to see all 
spaces from your instance.

````
AP.require('request', function(request) {
  request({
    url: '/rest/api/space',
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="get-specific-space"></a> Getting specific spaces from Confluence

This snippet lets you request a specific Confluence space by space key. In this example, we use `ds`. This recipe 
also provides some high-level information about the space. If you're looking for more information about 
a space, you can find out about the content in the space in the next example, using `/rest/api/space/{space.key}/content`.


````
AP.require('request', function(request) {
  request({
    url: '/rest/api/space/ds',
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="getting-space-pages"></a> Getting pages in a space

This recipe returns a collection for a given space identifier (like `ds` in the example below), containing 
objects like a blog post and one of the pages in the space.
This returns a collection for the given space identifier (e.g. `ds`) that contains a object of blog posts and one of
pages in the space. You can also directly access pages (`/rest/api/space/ds/content/page`) or 
blog posts (`/rest/api/space/ds/content/blogpost`) if you want to page though the contents. 

````
var space
AP.require('request', function(request) {
  request({
    url: '/rest/api/space/ds/content',
    success: function(response) {
      // convert the string response to JSON
      response = JSON.parse(response);

      // dump out the response to the console
      console.log(response);
    },
    error: function() {
      console.log(arguments);
    },
    contentType: "application/json"    
  });
});
````

### <a name="cookie-api"></a> Using the `cookie` API

This example creates a cookie through the [Connect API](../concepts/javascript-api.html) with a value of `nom nom nom`, 
and then retrieves that value. You can reference more on the [`cookie` API here](../javascript/module-cookie.html)

````
AP.require('cookie', function(cookie) {
  // set a cookie
  cookie.save('my-cookie', 'nom nom nom', 100);
  // read the cookie value back
  cookie.read('my-cookie', function(data) {
    // what's in my cookie?
    console.log(data);
    // remove the cookie now that we've read it
    cookie.erase('my-cookie');
  });
});
````

