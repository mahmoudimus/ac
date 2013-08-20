<p>
    Register a new Remotable Plugin

<div id="atlassian-connect-errors"></div>
<div id="rp-started">
    <p>Getting Started</p>
    <ul>
        <li><a href="https://bitbucket.org/mrdon/helloworldpage-app/overview">Hello World page example</a></li>
        <li><a href="{{contextPath}}/rest/atlassian-connect/latest/installer/schema/atlassian-plugin-remotable">XML descriptor schema</a></li>
        <li><a id="rp-keygen" href="javascript:void(0)">RSA key generator</a></li>
    </ul>

</div>
<div id="rp-install-form">
<form id="atlassian-connect-form" class="aui top-label">
    <fieldset>
        <div class="field-group">
            <label for="atlassian-connect-url">Registration URL</label>
            <input type="text" name="atlassian-connect-url" size="40" id="atlassian-connect-url"/>

            <div class="description">The URL to access to retrieve the
                <a href="{{contextPath}}/rest/atlassian-connect/latest/installer/schema/atlassian-plugin-remotable">descriptor XML</a>
                for the Remotable Plugin
            </div>
        </div>
    </fieldset>
</form>
</div>
<div id="rp-oauth-consumer">
<p>
    If you are configuring your app for this specific instance, this is the OAuth information for this consumer:
</p>
<form class="aui">
    <fieldset>
        <div class="field-group">
            <label>Consumer/Client Key</label>
            <span id="oauth-consumer-key">Loading...</span>
        </div>
        <div class="field-group">
            <label>Consumer/Client Public Key</label>
            <pre id="oauth-consumer-public-key"></pre>
        </div>
    </fieldset>
</form>
</div>
