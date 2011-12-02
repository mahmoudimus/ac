<p>
    Enter the registration URL for the Remote App that will return the descriptor XML.
    If you are interested in developing a new Remote App, see the
    sample app at the <a href="https://bitbucket.org/mrdon/remoteapps-plugin/src/master/sample">Bitbucket project</a>.
    If using the sample app on port 5432, enter the following in the text field below:
</p>
<pre>
    http://localhost:5432/user-register
</pre>
<div id="remoteapps-errors"></div>
<form id="remoteapps-form" class="aui top-label">
    <fieldset>
        <div class="field-group">
            <label for="remoteapps-url">Registration URL</label>
            <input type="text" name="remoteapps-url" size="40" id="remoteapps-url" />
        </div>
    </fieldset>
</form>
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
