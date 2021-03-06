# Upgrade atlassian-connect-express

If you are creating a new `atlassian-connect-express` (ACE) add-on, then all you need to do is follow the instructions in the
[readme of ACE](https://bitbucket.org/atlassian/atlassian-connect-express/).

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    Upgrading to ACE 0.9.x will replace OAuth 1 with JWT.
</div>

If you have an existing Connect add-on using ACE `0.8.x`, follow these steps to update.

1. Change the version of `atlassian-connect-express` to `~0.9.0` in `package.json`
2. Rename `atlassian-plugin.xml` to `atlassian-connect.json` per our [migration guide](./migrating-from-xml-to-json-descriptor.html)
Specific notes relating to ACE:
  * You _must_ have the lifecycle `installed` event registered to `/installed`
  * Update `routes/index.js` to return `atlassian-connect.json` instead of the xml descriptor
  * Apart from the `{{localBaseUrl}}` variable, `atlassian-connect.json` no longer supports substitution from `config.json`.
  Any substituted variables will need to be directly placed into your descriptor.
3. The function `addon.httpClient()` now returns a promise to the outgoing request, rather than the request directly.
By the time the promise is resolved the request is not writeable, so you must pass in any parameters as part of the
first argument to `get()`, `post()` etc.<br><br>
Additionally, we no longer support passing the method type by the 'method' parameter, instead you must call the
appropriate function on the `httpClient`. For example, code should be migrated from
```
    addon.httpClient(req)({
        method: 'POST',
        uri: '/some/url',
        json: data,
        userKey: userKey
    })
```
to:
```
    addon.httpClient(req).post({
        uri: '/some/url',
        json: data,
        userKey: userKey
    });
```
Note, that `httpClient` options take a __userKey__ object, rather than the __username__.
4. Run `npm install`
5. Test with `node app.js`

