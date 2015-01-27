# REST APIs

Atlassian products have a rich set of REST APIs for you to use.
These APIs are the way that you interact with the Atlassian application's features and data.

## Product APIs

[Atlassian REST API Browser](product-api-browser.html) provides an interactive interface to help you get acquainted with the available APIs.
Additionally, there is static REST API documentation available for [JIRA](https://docs.atlassian.com/jira/REST/latest) and [Confluence](https://docs.atlassian.com/confluence/REST/latest).

## Atlassian Connect

Atlassian Connect provides a set of REST APIs specifically designed for use by add-ons.

### Resources

<div class="ac-js-methods">
    <dl>
        <dt>
            <h3 class="name" id="get-addons-addonkey">
                <code class="method">GET</code>
                <code class="resource">/addons/{addonKey}</code>
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
</div>
