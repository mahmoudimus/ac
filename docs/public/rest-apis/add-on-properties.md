# Add-on properties

Add-on properties allow plugins to store and retrieve key value pairs. These properties are available only for requests coming from the owning add-on and can be used with both JIRA and Confluence.


## Resources

The following resources are available.

<div class="ac-js-methods">
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">GET</code>
                <code class="resource">/addons/{addonKey}/properties</code>
            </h3>
        </dt>
        <dd>
            <div class="class-description">
                <p>
                    Returns a list of property keys for the given add-on key.
                </p>
            </div>
            <h4>Parameters:</h4>
            <table class="params table table-striped aui">
                <thead>
                <tr>
                    <th>Location</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th class="last">Description</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>addonKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the add-on, as defined in its descriptor</p>
                        </td>
                    </tr>
                </tbody>
            </table>
            <h4>Response representations:</h4>
                <h5><code>200</code> - application/json</h5>
                <p>-----------------------------------</p>
                <div class="notrunnable example-container aui-buttons">
                    <textarea class="code">
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
}</textarea>
                </div>
                <h5><code>401</code> - application/json</h5>
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p> if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">GET</code>
                <code class="resource">/addons/{addonKey}/properties/{propertyKey}</code>
            </h3>
        </dt>
        <dd>
            <div class="class-description">
                <p>
                    Returns a property for the given property key.
                </p>
            </div>
            <h4>Parameters:</h4>
            <table class="params table table-striped aui">
                <thead>
                <tr>
                    <th>Location</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th class="last">Description</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>addonKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the add-on, as defined in its descriptor</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>propertyKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the property</p>
                        </td>
                    </tr>
                </tbody>
        </table>
        <h4>Response representations:</h4>
            <h5><code>200</code> - application/json</h5>
            <p>-----------------------------------</p>
            <div class="notrunnable example-container aui-buttons">
                <textarea class="code">
{
  "key" : "abcd",
  "value" : true,
  "self" : "/rest/api/atlassian-connect/${addOnKey}/properties/abcd"
}</textarea>
</div>
            <h5><code>400</code> - application/json</h5>
            <p>Property key longer than 255 characters.</p>
            <h5><code>401</code> - application/json</h5>
            <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p> if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
            <h5><code>404</code> - application/json</h5>
            <p>Request to get a property that does not exist.</p>
            <h5><code>404</code> - application/json</h5>
            <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
            add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">PUT</code>
                <code class="resource">/addons/{addonKey}/properties/{propertyKey}</code>
            </h3>
        </dt>
        <dd>
            <div class="class-description">
                <p>
                    Creates or updates a property.
                </p>
            </div>
            <h4>Parameters:</h4>
            <table class="params table table-striped aui">
                <thead>
                <tr>
                    <th>Location</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th class="last">Description</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>addonKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the add-on, as defined in its descriptor</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>propertyKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the property</p>
                        </td>
                    </tr>
                </tbody>
            </table>
            <h4>Response representations:</h4>
                <h5><code>200</code> - application/json</h5>
                <p>Property updated.</p>
                <h5><code>201</code> - application/json</h5>
                <p>Property created.</p>
                <h5><code>400</code> - application/json</h5>
                <p>Property key longer than 255 characters.</p>
                <h5><code>400</code> - application/json</h5>
                <p>Request made with invalid JSON.</p>
                <h5><code>401</code> - application/json</h5>
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p> if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
                <h5><code>404</code> - application/json</h5>
                <p>Request to get a property that does not exist.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">DELETE</code>
                <code class="resource">/addons/{addonKey}/properties/{propertyKey}</code>
            </h3>
        </dt>
        <dd>
            <div class="class-description">
                <p>
                    Deletes a property.
                </p>
            </div>
            <h4>Parameters:</h4>
            <table class="params table table-striped aui">
                <thead>
                <tr>
                    <th>Location</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th class="last">Description</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>addonKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the add-on, as defined in its descriptor</p>
                        </td>
                    </tr>
                    <tr>
                        <td class="location">
                            Path
                        </td>
                        <td class="name">
                            <code>propertyKey</code>
                        </td>
                        <td class="type">
                            <code>string</code>
                        </td>
                        <td class="description last">
                            <p>The key of the property</p>
                        </td>
                    </tr>
                </tbody>
            </table>
            <h4>Response representations:</h4>
                <h5><code>204</code> - application/json</h5>
                <p>Property deleted.</p>
                <h5><code>400</code> - application/json</h5>
                <p>Property key longer than 255 characters.</p>
                <h5><code>401</code> - application/json</h5>
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p> if the request was executed by anonymous user with AP.request or without a valid JWT token in the "Authorization" header or "jwt" query parameter
                <h5><code>404</code> - application/json</h5>
                <p>Request to get a property that does not exist.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
</div>

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

To make a put if absent request, the ETag has to be set to "".

