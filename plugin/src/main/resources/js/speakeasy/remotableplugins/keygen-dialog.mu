<p>
    If your Remotable Plugin will need to make authenticated requests to this host application, it will need a RSA key pair
    to sign the requests via OAuth.  The recommended approach is to generate your own keys, usually via openssl.
</p>
<p>
    As a convenience, the following is a newly generated key pair that you can use:
</p>
<form id="keygen-form" class="aui top-label">
    <fieldset>
        <div class="field-group">
            <label for="remotable-plugins-public-key">Public Key</label>
            <textarea name="public-key" cols="75" id="remotable-plugins-public-key" rows="7"></textarea>
        </div>
        <div class="field-group">
            <label for="remotable-plugins-private-key">Private Key</label>
            <textarea name="private-key" cols="75" id="remotable-plugins-private-key" rows="16"></textarea>
        </div>
    </fieldset>
</form>
<a id="keygen-close-link" href="javascript:void(0)">Close</a>
