# Upgrade atlassian-connect-express

If you are creating a new `atlassian-connect-express` (ACE) add-on, then all you need to do is follow the instructions in the
[readme of ACE](https://bitbucket.org/atlassian/atlassian-connect-express/).

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    These instructions do not include information for upgrading add-ons which are running in production systems.
    Coming soon!
</div>

If you have an existing Connect add-on using ACE `0.8.x`, follow these steps to update.

1. Change the version of `atlassian-connect-express` to `~0.9.0` in `package.json`
2. Rename `atlassian-plugin.xml` to `atlassian-connect.json` per our [migration guide](./migrating-from-xml-to-json-descriptor.html)
3. Run `npm install`
4. Test with `node app.js`