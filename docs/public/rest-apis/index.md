# REST APIs

Atlassian products have a rich set of REST APIs for you to use.
These APIs are the way that you interact with the Atlassian application's features and data.

## Product APIs

[Atlassian REST API Browser](product-api-browser.html) provides an interactive interface to help you get acquainted with the available APIs.
Additionally, there is static REST API documentation available for [JIRA](https://docs.atlassian.com/jira/REST/latest) and [Confluence](https://docs.atlassian.com/confluence/REST/latest).

## Atlassian Connect

Atlassian Connect provides a set of REST APIs specifically designed for use by add-ons.

### Index

* `/atlassian-connect/1/addons/{addonKey}` `[`[`GET`](#get-addons-addonkey)`]`
* `/atlassian-connect/1/addons/{addonKey}/properties` `[`[`GET`](#get-addons-addonkey-properties)`]`
* `/atlassian-connect/1/addons/{addonKey}/properties/{propertyKey}` `[`[`GET`](#get-addons-addonkey-properties-propertykey), [`PUT`](#put-addons-addonkey-properties-propertykey), [`DELETE`](#delete-addons-addonkey-properties-propertykey)`]`

### Resources

<div class="ac-js-methods">
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">GET</code>
                <code class="resource">.../addons/{addonKey}</code>
            </h3>
        </dt>
        <dd>
            <div class="class-description">
                <p>
                    Returns a representation of the add-on with the given key, including the state and license of the
                    add-on.
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
                <p>Request issued by enabled, licensed add-on.</p>
                <div class="notrunnable example-container aui-buttons">
                    <textarea class="code">
{
   "key": "example-addon",
   "version": "1.0",
   "state": "ENABLED",
   "host": {
      "product": "JIRA",
      "contacts": [
         {
            "name": "Example Contact",
            "email": "contact@example.com"
         }
      ]
   },
   "license": {
      "active": true,
      "type": "COMMERCIAL",
      "evaluation": false,
      "supportEntitlementNumber": "SEN-###"
   },
   "links": {
      "marketplace": [
         {
            "href": "http:// marketplace.atlassian.com/plugins/example-addon"
         }
      ],
      "self": [
         {
            "href": "http:// example.com/rest/atlassian-connect/latest/example-addon"
         }
      ]
   }
}</textarea>
                </div>
                <h5><code>200</code> - application/json</h5>
                <p>Request issued by enabled, unlicensed add-on.</p>
                <div class="notrunnable example-container aui-buttons">
                    <textarea class="code">
{
   "key": "example-addon",
   "version": "1.0",
   "state": "ENABLED",
   "host": {
      "product": "JIRA",
      "contacts": [
         {
            "name": "Example Contact",
            "email": "contact@example.com"
         }
      ]
   },
   "links": {
      "marketplace": [
         {
            "href": "http:// marketplace.atlassian.com/plugins/example-addon"
         }
      ],
      "self": [
         {
            "href": "http:// example.com/rest/atlassian-connect/latest/example-addon"
         }
      ]
   }
}</textarea>
                </div>
                <h5><code>200</code> - application/json</h5>
                <p>Request issued by disabled add-on.</p>
                <div class="notrunnable example-container aui-buttons">
                    <textarea class="code">
{
   "key": "example-addon",
   "version": "1.0",
   "state": "DISABLED"
}</textarea>
                </div>
                <h5><code>401</code> - application/json</h5>
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p>
                <h5><code>403</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself.</p>
            </div>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey-properties">
                <code class="method">GET</code>
                <code class="resource">.../addons/{addonKey}/properties</code>
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
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey-properties-propertykey">
                <code class="method">GET</code>
                <code class="resource">.../addons/{addonKey}/properties/{propertyKey}</code>
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
            <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p>
            <h5><code>404</code> - application/json</h5>
            <p>Request to get a property that does not exist.</p>
            <h5><code>404</code> - application/json</h5>
            <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
            add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="put-addons-addonkey-properties-propertykey">
                <code class="method">PUT</code>
                <code class="resource">.../addons/{addonKey}/properties/{propertyKey}</code>
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
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request to get a property that does not exist.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
    <dl>
        <dt>
            <h3 class="name" id="delete-addons-addonkey-properties-propertykey">
                <code class="method">DELETE</code>
                <code class="resource">.../addons/{addonKey}/properties/{propertyKey}</code>
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
                <p>Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request to get a property that does not exist.</p>
                <h5><code>404</code> - application/json</h5>
                <p>Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the
                add-on itself, or for a plugin that does not exist.</p>
        </dd>
    </dl>
</div>
