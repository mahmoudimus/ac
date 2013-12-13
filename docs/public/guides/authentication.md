# Authentication

Atlassian Connect employs usage of a technology called [JWT (JSON Web Token)](tools.ietf.org/html/draft-ietf-oauth-json-web-token‎)
 to authenticate add-ons. There is a nicely presented copy of the specification [here](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html).

## JWT

The JWT protocol describes the format and verification of individual JWT tokens, which are base-64 encoded UTF-8 strings. It does not prescribe a method
of transportation; we are transporting JWT tokens as query-string parameters and as authorization headers.

Query string example:

    GET http://ecosystem.atlassian.net/rest/api/2/issue/AC-1.json?jwt=<insert jwt here>

Headers example:

    POST http://ecosystem.atlassian.net/rest/api/2/issue/AC-1/attachments
    "Authorization" header value: "JWT <insert jwt here>"

The format of a JWT token is simple: ```<header>.<claims>.<signature>```.

* Each section is separated from the others by a period character (```.```).
* Each section is base-64 encoded, so you will need to decode each one to make them human-readable. There is a handy JWT decoder [here](https://py-jwt-decoder.appspot.com).
* The header specifies a very small amount of information that the receiver needs in order to parse and verify the JWT token.
 * All JWT token headers state that the type is "JWT".
 * The algorithm used to sign the JWT token is needed so that the receiver can verify the signature.
* The claims are a list of assertions that the issuer is making: each says that "this named field" has "this value".
 * Some, like the "iss" claim, which identifies the issuer of this JWT token, have standard names and uses.
 * Others are custom claims. We limit our use of custom claims as much as possible, for ease of implementation.
* The signature is computed by using an algorithm such as HMAC SHA-256 plus the header and claims sections.
 * The receiver verifies that the signature must have been computed using the genuine JWT header and claims sections, the indicated algorithm and a previously established secret.
 * An attacker tampering with the header or claims will cause signature verification to fail.
 * An attacker signing with a different secret will cause signature verification to fail.
 * There are various algorithm choices legal in the JWT spec. In atlassian-connect version 1.0 we support HMAC SHA-256.
 * While you may implement signing and signature verification yourself we recommend using a library.

### Example JWT token

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

Concatenating ```<encoded header>``` + ```.``` + ```<encoded claims>``` + ```.``` + ```<encoded signature>``` yields the JWT token above.

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

When communicating server-to-server with the Atlassian host product your add-on must include a JWT token when accessing an protected resources. This covers most of the REST APIs.

You may also find it useful to include tokens in iframe HTML for communication from the browser back to your add-on. For example, the URL of a link to an add-on resource could include a JWT token that the add-on uses to verify that it itself generated the link, and that therefore it can trust the link's query string parameters.

Construct a token that identifies your add-on, validates the query, limits the token's lifespan and allows the receiver to verify that this token was genuinely constructed by your add-on.

## Construct the Header

The header is a JSON object that looks like this example:

    {
        "alg": "HS256",
        "typ": "JWT"
    }

The "typ" field specifies that this is a JWT token. This is mandatory.

The "alg" field specifies the algorithm used to sign the token. In atlassian-connect version 1.0 we support the HMAC SHA-256 algorithm, which the [JWT specification](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-13) identifies using the string ["HS256"](http://tools.ietf.org/html/draft-ietf-jose-json-web-algorithms-18#section-3.1).

## Construct the Claims Set

The claims set is a JSON object that looks like this example:

    {
        "iss": "1234567890",
        "iat": 1386898951,
        "exp": 1386899131,
        "qsh": "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9",
        "sub": "a_user_key"
    }

Let's examine each of these claims.

* "iss": The issuer identifier. Use your add-on's client key that you received in the "installed" lifecycle callback.
* "iat": Issued-at time. The UTC Unix time when your add-on constructed this token. Used for detecting tokens illegally issued "in the future" and potentially in debugging the replay of outrageously old tokens.
* "exp" Expiration time. The UTC Unix time after which the receiver should NOT use this token. Used for limiting replays.
* "qsh": Query hash. A custom Atlassian claim that prevents URL tampering (described [above](#qsh)).
* "sub": The subject of this token. In atlassian-connect pre 1.0 we use this to identify the user key of the user that the add-on wishes to impersonate with this call; from 1.0 onwards this claim will be ignored, as user impersonation will not be possible.

## Encode and Sign

First convert the header and claims set JSON objects to UTF-8 encoded strings and base-64 encode each of them.

Second, concatenate the encoded header, a period character (```.```) and the encoded claims set. This yields the input to the signing algorithm.

Third, compute the signature using the JWT or cryptographic library of your choice.

Finally, concatenate the signing input, another period character and the signature.

Here is an example in Java using json-smart, guava and commons-codec:

    import com.google.common.collect.ImmutableMap;
    import net.minidev.json.JSONObject;
    
    import javax.crypto.Mac;
    import javax.crypto.SecretKey;
    import javax.crypto.spec.SecretKeySpec;
    import java.security.InvalidKeyException;
    import java.security.NoSuchAlgorithmException;
    
    import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

    public String jsonToHmacSha256Jwt(JSONObject claimsSet) throws NoSuchAlgorithmException, InvalidKeyException
    {
        String jwtHeader = new JSONObject(ImmutableMap.of("alg", "HS256", "typ", "JWT")).toJSONString();
        String signingInput = encodeBase64URLSafeString(jwtHeader.getBytes()) + "." + encodeBase64URLSafeString(claimsSet.toJSONString().getBytes());
        return signingInput + "." + signHmac256(signingInput);
    }

    public String signHmac256(String signingInput) throws NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKey key = new SecretKeySpec("shared secret".getBytes(), HMAC_SHA_256);
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(key);
        return encodeBase64URLSafeString(mac.doFinal(signingInput.getBytes()));
    }

Here is an example in Java using nimbus-jose-jwt:

    import com.nimbusds.jose.*;
    import com.nimbusds.jose.crypto.MACSigner;

    public String jsonToJwt(String claimsSetAsJsonString) throws JOSEException
    {
        // Serialise JWS object to compact format
        return generateJwsObject(claimsSetAsJsonString).serialize();
    }

    private JWSObject generateJwsObject(String claimsSetAsJsonString) throws JOSEException
    {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setType(new JOSEObjectType(JWT));

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSetAsJsonString));
        jwsObject.sign(new MACSigner("shared secret"));
        
        return jwsObject;
    }
