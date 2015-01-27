# Add-on properties

Add-on properties allow plugins to store and retrieve key value pairs. These properties are available only for requests coming from the owning add-on and can be used with both JIRA and Confluence.


## Resources

The following resources are available.

### List all properties

    GET /rest/atlassian-connect/{version}/addons/{addOnKey}/properties

#### Path parameters

* `{version}` - current API version (1)
* `{addOnKey}` - add-on key for which to list the properties

#### Response Body

##### Success
    {
        "keys" : [
              {
                "key" : "first_key",
                "self" : "/rest/api/atlassian-connect/${addOnKey}/properties/first_key"
              },
              {
                  "key" : "another_key",
                  "self" : "/rest/api/atlassian-connect/${addOnKey}/properties/another_key"
              }
        ]
    }
##### Error Responses

* **400** - if the property key is longer than 255 characters
* **401** - if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
* **404** - if the property with given key does not exist
* **404** - if no plugin with given add-on key exists or the request has been made from another plugin

### Get property

    GET /rest/atlassian-connect/{version}/addons/{addOnKey}/properties/{key}

#### Path parameters

* `{version}` - current API version (1)
* `{addOnKey}` - add-on key from which to get the property
* `{key}` - property key to get

#### Response Body

##### Success
    {
      "key" : "abcd",
      "value" : true,
      "self" : "/rest/api/atlassian-connect/${addOnKey}/properties/abcd"
    }

##### Error Responses

* **400** - if the property key is longer than 255 characters
* **401** - if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
* **404** - if the property with given key does not exist
* **404** - if no plugin with given add-on key exists or the request has been made from another plugin

### Create or update property

    PUT /rest/atlassian-connect/{version}/addons/{addOnKey}/properties/{key}

#### Path parameters

* `{version}` - current API version (1)
* `{addOnKey}` - add-on key from which to get the property
* `{key}` - property key to get

#### Example Input body

    {
      "key" : "abcd",
      "value" : true,
      "self" : "/rest/api/atlassian-connect/${addOnKey}/properties/abcd"
    }

#### Response Body

##### Success

* **200** - if the property has been updated
* **201** - if the property has been created

##### Error Responses

* **400** - if the property key is longer than 255 characters
* **400** - if the value is not a valid json
* **401** - if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
* **404** - if the property with given key does not exist
* **404** - if no plugin with given add-on key exists or the request has been made from another plugin

### Delete property

    DELETE /rest/atlassian-connect/{version}/addons/{addOnKey}/properties/{key}

#### Path parameters

* `{version}` - current API version (1)
* `{addOnKey}` - add-on key from which to get the property
* `{key}` - property key to get

#### Response Body

##### Success

* **204** - if the property was successfully deleted

##### Error Responses

* **400** - if the property key is longer than 255 characters
* **401** - if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
* **404** - if the property with given key does not exist
* **404** - if no plugin with given add-on key exists or the request has been made from another plugin

The above endpoints do not require any scope to be defined in the plugin descriptor.

## Limitations

The data is limited to 100 properties, with a single property up to 32KB in size.
The properties have to be in a valid JSON format.
Requests via [`AP.request`](../javascript/module-request.html) to store and receive properties can only be made with a logged in user.

## Request example

Here is an example snippet that will show a popup with a json property named my-property-key for add-on with key my-add-on-key.

     AP.require(['request'], function(request) {
         request({
             url: '/rest/api/1/atlassian-connect/my-add-on-key/properties/my-property-key',
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
             url: '/rest/api/1/atlassian-connect/my-add-on-key/properties/my-property-key',
             success: function(responseText, textStatus, jqXHR) {
                 eTag = jqXHR.getResponseHeader("ETag");
                 alert("ETag = " + jqXHR.getResponseHeader("ETag"));
             },
             type: 'PUT',
             contentType: "application/json"
         });
     });

Executed by User 2

    AP.require(['request'], function(request) {
         request({
             url: '/rest/api/1/atlassian-connect/my-add-on-key/properties/my-property-key',
             success: function(responseText) {
                 response = JSON.parse(responseText);
                 alert(response);
             },
             type: 'PUT',
             contentType: "application/json"
         });
     });

Executed by User 1 with Entity Tag from previous PUT request.

     AP.require(['request'], function(request) {
          request({
              url: '/rest/api/1/atlassian-connect/my-add-on-key/properties/my-property-key',
              success: function(responseText, textStatus, jqXHR) {
                    response = JSON.parse(responseText);
                    alert(response);
                  }
              },
              error: function(responseText, textStatus, jqXHR) {
                  if (jqXHR.status == 412) {
                    alert("Precondition failed!");
                  } else {
                    console.log("Error loading API (" + uri + ")");
                    console.log(arguments);
                  }
              },
              type: 'PUT',
              headers: {If-Match : ETag},
              contentType: "application/json"
          });
          });

The second request will result in a response status 412 - Precondition Failed.

