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

## Request example

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
