# Authentication

Atlassian Connect employs usage of a technology called [JWT (JSON Web Token)](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html)
 to authenticate add-ons.

## Implementation Examples

Most modern languages have JWT libraries available. Prior to implementing JWT, consider using an existing library.

* Java (Atlassian) - [atlassian-jwt](https://bitbucket.org/atlassian/atlassian-jwt/)
* Java - [jsontoken](https://code.google.com/p/jsontoken/)
* Node.js - [node-jwt-simple](https://github.com/hokaccha/node-jwt-simple)
* Ruby - [ruby-jwt](https://github.com/progrium/ruby-jwt)
* PHP - [jwt](https://github.com/luciferous/jwt)
* .NET - [jwt](https://github.com/johnsheehan/jwt)

The [py-jwt-decoder](http://py-jwt-decoder.appspot.com/) is a handy web based decoder of JWT tokens.

<a name='installation'></a>
# Installation Handshake

For an Atlassian Connect add-on to authenticate securely with the host Atlassian product, it must advertise itself as
being JWT aware, and provide a resource to receive important installation information. This is done by specifying the
elements `authentication` and `lifecycle`.

For example:

    {
        "baseUrl": "http://localhost:3000",
        "key": "atlassian-connect-addon",
        "authentication": {
            "type": "jwt"
        },
        "lifecycle": {
            "installed": "/add-on-installed-callback"
        }
        modules: {} // etc
    }


The `lifecycle:installed` property is a url which is synchronously called by the Atlassian application when the add-on
is installed. The request contains a payload with important tenant information that you will need to store in your add-on
in order to sign and verify future requests. The payload contains the following attributes:

    {
        "key": "atlassian-connect-jira-addon-jwt",
        "clientKey": "1234567890",
        "publicKey": "MIGf....ZRWzwIDAQAB",
        "sharedSecret": "1ad6f705-fe0b-4111-9551-7ce5d81d2884",
        "baseUrl": "http://storm.dyn.syd.atlassian.com:2990/jira",
        "productType": "jira",
        "userKey": "admin",
        "eventType": "installed"
    }


<div class="aui-message warning">
    <p class="title">
        <span class="aui-icon icon-warning"></span>
        <strong>Important</strong>
    </p>
    Upon successful registration, the add-on must return either a `200 OK` or `204 No Content` response code, otherwise
    the operation will fail and the installation will be marked as incomplete.
</div>

## Details

<table class='aui'>
    <thead>
        <tr>
            <th>Attribute</th>
            <th>Description</th>
        </tr>
    </thead>
    <tr>
        <td>`key`</td>
        <td>Add-on key that was installed into the Atlassian Product</td>
    </tr>
    <tr>
        <td>`clientKey`</td>
        <td>Identifying key for the instance that the add-on was installed into. This will never change for a given
        instance, and is unique across all Atlassian OnDemand tenants. This value should be used to key tenant details
        in your add-on.</td>
    </tr>
    <tr>
        <td>`publicKey`</td>
        <td>This is the public key for the Atlassian OnDemand application.</td>
    </tr>
    <tr>
        <td>`sharedSecret`</td>
        <td></td>
    </tr>
    <tr>
        <td>`baseUrl`</td>
        <td></td>
    </tr>
    <tr>
        <td>`productType`</td>
        <td></td>
    </tr>
</table>

<a name='incoming'></a>
# Validating Incoming Requests

All incoming requests should check for the presence of the `jwt` query string parameter, which needs to be decoded and
verified. An incoming request might look like:

    GET /hello-world?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEzODY4OTM4OTYsImlzcyI6ImppcmE6MTU0ODk1OTUiLCJxc2giOiI4MDYzZmY0Y2ExZTQxZGY3YmM5MGM4YWI2ZDBmNjIwN2Q0OTFjZjZkYWQ3YzY2ZWE3OTdiNDYxNGI3MTkyMmU5IiwiaWF0IjoxMzg2ODkzNzE2fQ.2hJD79ZF0dauaczjfn42f3KaKVYi006u7mDzAO18FoA


1. Extract the JWT token from the request's `jwt` query parameter
2. Decode the JWT token, without verification.
3. Inspect the decoded, unverified token for the `iss` ([token issuer](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-13#section-4.1.1))
claim. This is the `clientKey` for the tenant.
4. Look up the `sharedSecret` for the `clientKey`. This should have been stored as part of the [installation handshake](#installation)
process.
5. Decode the same JWT token, this time verifying the signature with the `sharedSecret`.

<a name='outgoing'></a>
# Signing Outgoing Requests
