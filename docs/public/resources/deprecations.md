# Deprecation Notices

### XML descriptor

The `atlassian-plugin.xml` descriptor format is deprecated in favour of the new JSON descriptor format.

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
        <h5>OnDemand removal</h5>
    </div>
    <div class="aui-item">
        __28th February, 2014__
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
        <h5>OnDemand removal</h5>
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
        If you are using Atlassian Connect Express, <a href="../guides/upgrade-ace.html">upgrade to version 0.9.0</a>.<br>
        Otherwise, follow our <a href="./authentication.html">authentication guide</a> to implement JWT.
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
        <h5>OnDemand removal</h5>
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
