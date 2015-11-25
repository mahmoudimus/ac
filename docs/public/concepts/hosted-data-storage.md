# Hosted data storage

Add-on properties allow add-ons to store key-value pairs in the host application, either JIRA or Confluence, and retrieve them.
All operations on the properties require authentication by the owning add-on.

## Supported Operations

* [List properties](../rest-apis/index.html#get-addons-addonkey-properties)
* [Get property](../rest-apis/index.html#get-addons-addonkey-properties-propertykey)
* [Create or update property](../rest-apis/index.html#put-addons-addonkey-properties-propertykey)
* [Delete property](../rest-apis/index.html#delete-addons-addonkey-properties-propertykey)

## Limitations

The data is limited to 100 properties, with a single property up to 32KB in size.
The properties have to be in a valid JSON format.
Requests via [`AP.request`](../javascript/module-request.html) to store and receive properties can only be made with a logged-in user.

**Warning:** Add-on properties can be manipulated by a malicious authenticated user (e.g. by making REST calls through the developer console). For this reason:

 * Don't store user-specific data in add-on properties (particularly sensitive data).
 * Be defensive when retrieving add-on properties, and don't assume data consistency (arbitrary keys may be modified or deleted by users).

## Request example

To give you a very basic example you can set an add-on property like so:

    PUT /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    {"string":"string-value","number":5}

And if you were to request it again you would get the following response:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property?jsonValue=true
    {"key":"test-property","value":{"string":"string-value","number":5},"self":"..."}
    
Please note the use of the `jsonValue=true` query param. If you do not include that parameter then the response will be:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property?jsonValue=true
    {"key":"test-property","value":"{\"string\":\"string-value\",\"number\":5}","self":"..."}
     
Which is deprecated behaviour and will be going away in June 2016. Please always use the `jsonValue=true` query parameter.

Here is an example snippet that will show a pop-up with a JSON property named my-property-key for add-on with key my-add-on-key.

     AP.require(['request'], function(request) {
         request({
             url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key',
             success: function(response) {
                 // Convert the string response to JSON
                 response = JSON.parse(response);
                 alert(response);
             },
             error: function(response) {
                 console.log("Error loading API (" + uri + ")");
                 console.log(arguments);
             },
             contentType: "application/json"
         });
     });

Apart from using [`AP.request`](../javascript/module-request.html), the same endpoints are accessible via a request signed with JWT.
