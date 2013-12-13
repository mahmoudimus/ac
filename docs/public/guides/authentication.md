# Authentication

Atlassian Connect employs usage of a technology called [JWT (JSON Web Token)](tools.ietf.org/html/draft-ietf-oauth-json-web-token‎)
 to authenticate add-ons. There is a nicely presented copy of the specification [here](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html).

## JWT

The JWT protocol describes the format and verification of individual JWT messages, which are base-64 encoded UTF-8 strings. It does not prescribe a method
of transportation; we are transporting JWT messages as query-string parameters and as authorization headers.

Query string example:

    GET http://ecosystem.atlassian.net/rest/api/2/issue/AC-1.json?jwt=<insert jwt here>

Headers example:

    POST http://ecosystem.atlassian.net/rest/api/2/issue/AC-1/attachments
    "Authorization" header value: "JWT <insert jwt here>"

The format of a JWT message is simple: ```<header>.<claims>.<signature>```.

* Each section is separated from the others by a period character (```.```).
* Each section is base-64 encoded, so you will need to decode each one to make them human-readable. There is a handy JWT decoder [here](https://py-jwt-decoder.appspot.com).
* The header specifies a very small amount of information that the receiver needs in order to parse and verify the JWT message.
 * All JWT message headers state that the type is "JWT".
 * The algorithm used to sign the JWT message is needed so that the receiver can verify the signature.
* The claims are a list of assertions that the issuer is making: each says that "this named field" has "this value".
 * Some, like the "iss" claim, which identifies the issuer of this JWT message, have standard names and uses.
 * Others are custom claims. We limit our use of custom claims as much as possible, for ease of implementation.
* The signature is computed by using an algorithm such as HMAC SHA-256 plus the header and claims sections.
 * The receiver verifies that the signature must have been computed using the genuine JWT header and claims sections, the indicated algorithm and a previously established secret.
 * An attacker tampering with the header or claims will cause signature verification to fail.
 * An attacker signing with a different secret will cause signature verification to fail.
 * There are various algorithm choices legal in the JWT spec. In atlassian-connect version 1.0 we support HMAC SHA-256.
 * While you may implement signing and signature verification yourself we recommend using a library.

### Example JWT message

    eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk

That looks like a big blob of characters, so let's break it down.

#### Headers

The following example JWT Header declares that the encoded object is a JSON Web Token (JWT) and the JWT is a JWS that is MACed using the HMAC SHA-256 algorithm:

    {"typ":"JWT",
     "alg":"HS256"}

Base-64 encoding the UTF-8 representation of this header yields:

    eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9

#### Claims

The following example claims set says that the issuer is "joe", the message expires at "Tue, 22 Mar 2011 18:43:00 GMT" and that the custom claim "http://example.com/is_root" has the value "true":

    {"iss":"joe",
     "exp":1300819380,
     "http://example.com/is_root":true}

Base-64 encoding its UTF-8 characters yields:

    eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ

#### Signature

Computing the MAC of the encoded JWS Header and encoded JWS Payload with the HMAC SHA-256 algorithm and base64url encoding the HMAC value yields:

    dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk

Concatenating ```<encoded header>``` + ```.``` + ```<encoded claims>``` + ```.``` + ```<encoded signature>``` yields the JWT message above.

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
verified.

1. Extract the JWT token from the request's `jwt` query parameter
2. Decode the JWT token, without verification
3. Inspect the decoded, unverified token for the `iss` ([token issuer](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-13#section-4.1.1))
claim. This is the `clientKey` for the tenant
4. Look up the `sharedSecret` for the `clientKey`. This should have been stored as part of the [installation handshake](#installation)
process
5. Decode the same JWT token, this time verifying the signature with `SHA-256` algorithm and the `sharedSecret`
6. Verify the query string by [creating a query string hash](#qsh) and comparing against the `qsh` claim on the verified token.

## Example

An incoming request might look like:

    GET /hello-world?lic=none&tz=Australia%2FSydney&cp=%2Fjira&user_key=&loc=en-US&user_id=&jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEzODY4OTkxMzEsImlzcyI6ImppcmE6MTU0ODk1OTUiLCJxc2giOiI4MDYzZmY0Y2ExZTQxZGY3YmM5MGM4YWI2ZDBmNjIwN2Q0OTFjZjZkYWQ3YzY2ZWE3OTdiNDYxNGI3MTkyMmU5IiwiaWF0IjoxMzg2ODk4OTUxfQ.uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo&xdm_e=http%3A%2F%2Fstorm%3A2990&xdm_c=channel-servlet-hello-world&xdm_p=1

### 1. Decode the `jwt` token:

    jwtToken = request.getParameter('jwt');
    unverifiedClaims = base64decode(jwtToken);
    segments = unverifiedClaims.split('.');
    headerSegment = segments[0];
    payloadSegment = segments[1];
    signatureSegment = segments[2];

Header Segment

    {
        "alg": "HS256",
        "typ": "JWT"
    }

Payload Segment

    {
        "iss": "jira:15489595",
        "iat": 1386898951,
        "qsh": "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9",
        "exp": 1386899131
    }

Signature Segment

    uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo

Look up the `sharedSecret` for the `iss` (issuer).

### 2. Verify the `jwt` token with the shared secret

    signingInput = [headerSegment, payloadSegment].join('.');
    expectedSignature = sign(signingInput, sharedSecret, signingMethod);

    if (expectedSignature !== signatureSegment) {
        throw new Error('Signature verification failed');
    }

### 3. Verify query string hash

Verify the query string by [creating a query string hash](#qsh) for the request and comparing against the `qsh` claim
on the verified token.

<a name='outgoing'></a>
# Signing Outgoing Requests

<a name='qsh'></a>
# Creating a query string hash

### Overview
query signature = `sign(canonical-request)`

canonical-request = `canonical-method + '&' + canonical-URI + '&' + canonical-query-string`

### Method

1. Compute canonical method
  *  Simply the upper-case of the method name (e.g. `"GET"` or `"PUT"`)<br><br>
2. Append the character `'&'`<br><br>
3. Compute canonical URI
  *  Discard the protocol, server, port, context path and query parameters from the full URL.
  *  Removing the context path allows a reverse proxy to redirect incoming requests for `"jira.example.com/getsomething"`
   to `"example.com/jira/getsomething"` without breaking authentication. The requester cannot know that the reverse proxy
   will prepend the context path `"/jira"` to the originally requested path `"/getsomething"`
  *  Empty-string is not permitted; use `"/"` instead.
  *  Do not suffix with a `'/'` character unless it is the only character. e.g.
     *  Canonical URI of `"http://server:80/some/path/?param=value"` is `"/some/path"`
     *  Canonical URI of `"http://server:80"` is `"/"`
    <br><br>
4. Append the character `'&'`<br><br>
5. Compute canonical query string
  *  Sort the query parameters primarily by their percent-encoded names and secondarily by their percent-encoded values
  *  Sorting is by codepoint: `sort(["a", "A", "b", "B"]) => ["A", "B", "a", "b"]`
  *  For each parameter append its percent-encoded name, the `'='` character and then its percent-encoded value.
  *  In the case of repeated parameters append the `','` character and subsequent percent-encoded values.
  *  Ignore the `jwt` parameter, if present.
  *  Some particular values to be aware of:
    *  `"+"` is encoded as `"%20"`,
    *  `"*"` as `"%2A"` and
    *  `"~"` as `"~"`.<br>
    (These values used for consistency with OAuth1.)

  An example: for a `GET` request to the not-yet-percent-encoded URL

        "http://localhost:2990/path/to/service?zee_last=param&repeated=parameter 1&first=param&repeated=parameter 2"

  the canonical request is

        "GET&/path/to/service&first=param&repeated=parameter%201,parameter%202&zee_last=param"

6. Convert the canonical request string to bytes
   *  The encoding used to represent characters as bytes is `UTF-8`<br><br>
7. Hash the canonical request bytes using the `SHA-256` algorithm
   * e.g. The `SHA-256` hash of `"foo"` is `"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"`
