# Add-on properties

Add-on properties allow plugins to store and retrieve key value pairs. These properties are available only for requests coming from the owning add-on and can be used with both JIRA and Confluence.

## Limitations

The data is limited to 100 properties, with a single property up to 32KB in size.
The properties have to be in a valid JSON format.
Requests via [`AP.request`](../javascript/module-request.html) to store and receive properties can only be made with a logged in user.

## Request example

Here is an example snippet that will show a popup with a json property named my-property-key for add-on with key my-add-on-key.

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