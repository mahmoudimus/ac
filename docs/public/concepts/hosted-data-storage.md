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

Apart from using AP.request(link), the same endpoints are accessible via a request signed with JWT.


## Optimistic locking

Furthermore, the above endpoints return an ETag which when used in the If-Match and If-None-Matched headers adds additional functionality.

* For `GET` operations, if the resource has the same ETag as the one defined in If-None-Match, the resulting code will be **304** - Not Modified.
* For `PUT` and `DELETE`, if the resource has a different ETag as the one defined in If-Match, the resulting code will be **412** - Precondition Failed.


## Optimistic locking example

Executed by User 1:

    var eTag;
    AP.require(['request'], function(request) {
         request({
             url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key',
             success: function(responseText, textStatus, jqXHR) {
                 eTag = jqXHR.getResponseHeader("ETag");
                 alert("ETag = " + jqXHR.getResponseHeader("ETag"));
             },
             type: 'PUT',
             data: "5",
             contentType: "application/json"
         });
     });

Executed by User 2

    AP.require(['request'], function(request) {
         request({
             url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key',
             success: function(responseText) {
                 response = JSON.parse(responseText);
                 alert(response);
             },
             type: 'PUT',
             data: "10",
             contentType: "application/json"
         });
     });

Executed by User 1 with Entity Tag from previous PUT request.

     AP.require(['request'], function(request) {
          request({
              url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key',
              success: function(responseText, textStatus, jqXHR) {
                  response = JSON.parse(responseText);
                  alert(response);
              },
              error: function(responseText, textStatus, jqXHR) {
                  if (jqXHR.status == 412) {
                      alert("Precondition failed!");
                  } else {
                      console.log("Error with status code: " + jqXHR.status);
                  }
              },
              type: 'PUT',
              data: "15",
              headers: {If-Match : ETag},
              contentType: "application/json"
          });
          });

The second request will result in a response status 412 - Precondition Failed.

To make a put if absent request, the ETag has to be set to "".