<p>
    Register a new Remote App

<div id="remoteapps-errors"></div>
<div id="rp-started">
    <p>Getting Started</p>
    <ul>
        <li><a href="https://bitbucket.org/mrdon/helloworldpage-app/overview">Hello World page example</a></li>
        <li><a href="{{contextPath}}/rest/remoteapps/latest/installer/schema/remote-app">XML descriptor schema</a></li>
        <li><a id="rp-keygen" href="javascript:void(0)">RSA key generator</a></li>
    </ul>

</div>
<div id="rp-install-form">
<form id="remoteapps-form" class="aui top-label">
    <fieldset>
        <div class="field-group">
            <label for="remoteapps-url">Registration URL</label>
            <input type="text" name="remoteapps-url" size="40" id="remoteapps-url"/>

            <div class="description">The URL to access to retrieve the
                <a href="{{contextPath}}/rest/remoteapps/latest/installer/schema/remote-app">descriptor XML</a>
                for the Remote App
            </div>
        </div>
        <div class="field-group">
            <label for="remoteapps-token">Registration Secret (optional)</label>
            <input type="text" name="remoteapps-token" size="40" id="remoteapps-token"/>

            <div class="description">The secret password to send to the Remote App that may be required for
                registration
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
