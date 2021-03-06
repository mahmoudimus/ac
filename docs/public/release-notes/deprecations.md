# Deprecation Notices

### /license REST API resource

Description
<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.1.21</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __August, 2015__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        Use the newly available REST API resource [`/addons/{addonKey}`](../rest-apis#get-addons-addonkey) instead.
    </div>
</div>
</div>


### XML descriptor

The `atlassian-plugin.xml` descriptor format is deprecated in favour of the new JSON descriptor format. XML descriptor
add-ons will still function and be supported until the deprecation date, however no new XML based add-ons will be
approved after Atlassian Connect 1.0.0 is released.

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.0-m25</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __May, 2014__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        <a href="../guides/migrating-from-xml-to-json-descriptor.html">Migrating to the JSON descriptor</a>
    </div>
</div>
</div>

### OAuth 1.0 2LO

Atlassian Connect is replacing the OAuth 1.0 2LO implementation with JWT.

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.0-m25</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __August, 2015__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        If you are using Atlassian Connect Express, <a href="../guides/upgrade-ace.html">upgrade to version 0.9.0</a>.<br>
        Otherwise, follow our <a href="./authentication.html">authentication guide</a> to implement JWT.
    </div>
</div>
</div>


### Opening dialogs by URL

JSON descriptor based add-ons can no longer open dialogs using a `url` parameter, for instance with `dialog.create({url: 'my-contents.html'})`.

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.0-rc2</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __24th March, 2014__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        <p>We recommend using <code>dialog.create({key: 'my-module-key',...})</code>
        instead (see <a href="../javascript/module-Dialog.html">Javascript Dialog documentation</a>).</p>
        <p>The key references a general page (or a web item).
        In order to prevent the general page or web item to show up anywhere else, you can use the <code>"location"</code>
        property with a value of <code>"none"</code>.</p>
    </div>
</div>
</div>


### Email sender resource

Atlassian Connect will no longer provide a REST endpoint allowing add-ons to send emails to users as either JIRA or Confluence.

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.0-m26</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __10th February, 2014__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        We recommend either:
        <ul>
            <li>building an email sender into your add-on</li>
            <li>using an email sending service, such as <a href="https://devcenter.heroku.com/articles/sendgrid" target="_blank">SendGrid</a>
                or <a href="https://addons.heroku.com/postmark" target="_blank">Postmark</a>.</li>
        </ul>
    </div>
</div>
</div>


### JIRA REST API

Both global and project permissions are changing from integer based to a key based representation. The following REST resources are affected: 
<ul>
    <li>
        /rest/api/v2/mypermissions<br/>
        A `type` attribute has been added to each permission, indicating whether it is a `PROJECT` or `GLOBAL` one. <br/>
        The `id` attribute is being deprecated and will be removed in the future.
    </li>
</ul>

<div class="ac-deprecations">
    <div class="aui-group">
        <div class="aui-item ac-property-key">
            <h5>Deprecated in</h5>
        </div>
        <div class="aui-item">
            <span class="aui-lozenge">Jira 7.0-OD4</span>
        </div>
    </div>
    <div class="aui-group">
        <div class="aui-item ac-property-key">
            <h5>Atlassian Cloud removal</h5>
        </div>
        <div class="aui-item">
            __January, 2015__
        </div>
    </div>
    <div class="aui-group">
        <div class="aui-item ac-property-key">
            <h5>Upgrade guide</h5>
        </div>
        <div class="aui-item">
            We recommend identifying a permission by its `key` instead of its `id`. 
            `GlobalPermissionKey` and `ProjectPermissionKey` are the key types for global and project permissions respectively.
        </div>
    </div>
</div>

### Confluence `page.*` context variables

The `page.id`, `page.version`, `page.type` context variables available in Confluence have been deprecated in favour of
`content.id`, `content.version` and `content.type` variables respectively.

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.1.0-final</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __2015__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        <p>Use the newly available `content.*` variables, documented in <a href="../concepts/context-parameters.html">Context Parameters</a>.</p>
    </div>
</div>
</div>

## Add-on properties 'value' field is being unescaped

Requests to the following resource:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    
Return json objects in the following format:

    {
       "key": "my-property",
       "value": "{\"object\": \"data\"}",
       "self": "http://<self-url>..."
    }

Starting immediately the addition of the `?jsonValue=true` query parameter to the response will result in the following
response:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property?jsonValue=true
    {
       "key": "my-property",
       "value": {"object": "data"},
       "self": "http://<self-url>..."
    }
    
You will need to swap over to using the `jsonValue` query param over the next six months. After that time the `value`
field will always populate as though the `?jsonValue=true` was set. 

<div class="ac-deprecations">
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Deprecated in</h5>
    </div>
    <div class="aui-item">
        <span class="aui-lozenge">1.1.65</span>
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Atlassian Cloud removal</h5>
    </div>
    <div class="aui-item">
        __June 2016__
    </div>
</div>
<div class="aui-group">
    <div class="aui-item ac-property-key">
        <h5>Upgrade guide</h5>
    </div>
    <div class="aui-item">
        <p>Please swap over to using jsonValue query param over the next six months and adjust your code to handle the updated format.</p>
    </div>
</div>
</div>