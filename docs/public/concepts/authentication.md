# Authentication

## Getting started

The purpose of this section is to describe how an add-on can authenticate with Connect when making API calls 
to Atlassian products or exposing endpoints called by an Atlassian product. Atlassian Connect uses a technology called [JWT (JSON Web Token)](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token‎) to authenticate add-ons. Basically a security 
context is exchanged when the add-on is installed, and this context is used to create and validate JWT tokens, embedded in API calls. 
The use of JWT tokens guarantees that:

 * The Atlassian application can verify it is talking to the add-on, and vice versa (authenticity).
 * None of the query parameters of the HTTP request, nor the path (excluding the context path), nor the HTTP method, 
 were altered in transit (integrity).

Here is how your add-on can leverage Connect's authentication feature:
 
1. You declare that the add-on uses JWT as the authentication mechanism in the add-on descriptor.
2. You implement an installation callback endpoint, and add a reference to it in the add-on descriptor. 
 * When an administrator installs the add-on on cloud instances, Connect initiates an "installation handshake": it invokes the endpoint, 
 passing a security context. You must then store this security context for future use.
 * The security contexts contains, among other things, a key identifying the add-on and a shared secret (used to create and validate JWT tokens).
3. You then use the security context to validate incoming requests (e.g. Webhooks), and sign outgoing requests (e.g. REST API calls to JIRA).
 
### Installation handshake

The installation handshake is a way for the Atlassian application and the add-on to exchange keys stored on both sides for future API calls.

<div class="diagram">
participant Administrator
participant Browser
participant Atlassian\nProduct
participant Add_on
Administrator->Atlassian\nProduct: Install Add-on
Atlassian\nProduct->Add_on: Retrieve Addon Descriptor\n(advertised as being JWT aware)
Atlassian\nProduct->Add_on: Installation Handshake \n(contains a security context)
Add_on->Add_on: Store\nsecurity\ncontext
Add_on->Atlassian\nProduct: 
Atlassian\nProduct->Browser: Confirmation\npage
Browser->Administrator: Page\nrendered
</div>

### Making a service call

When an add-on calls an API exposed by an Atlassian product, it must add a valid JWT token to the request, 
created using the security context provided during the installation handshake.

<div class="diagram">
participant Add_on
participant Atlassian\nProduct
Add_on->Add_on: Retrieve security context
Add_on->Add_on: Create JWT Token
Add_on->Atlassian\nProduct: Send Request (contains JWT token)
Atlassian\nProduct->Atlassian\nProduct: Validate JWT Token
Atlassian\nProduct->Atlassian\nProduct: Process Request
Atlassian\nProduct->Add_on: Response
</div>

### Exposing a service

When an Atlassian product calls an API exposed by the add-on, it is the add-on's responsibility to validate 
the JWT token, using the security context provided during the installation handshake.

<div class="diagram">
participant Atlassian\nProduct
participant Add_on
Atlassian\nProduct->Add_on: Request (contains a JWT token)
Add_on->Add_on: Retrieve security context
Add_on->Add_on: Decode and Validate JWT Token
Add_on->Add_on: Process Request
Add_on->Atlassian\nProduct: Response
</div>


## Authentication how-to

### Creating the add-on descriptor

For an Atlassian Connect add-on to authenticate securely with the host Atlassian product, it must advertise itself as
being JWT aware, and provide a resource to receive important installation information. This is done by specifying the
elements `authentication` and `lifecycle`.

The `lifecycle:installed` property is a url which is synchronously called by the Atlassian application when the add-on
is installed. 

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
        "modules": {} // etc
    }


<a name='installation'></a>
### Installation handshake

When the add-on is installed, the Atlassian application invokes a callback endpoint exposed by the add-on. 
The request contains a payload with important tenant information that you will need to store in your add-on in 
order to sign and verify future requests. The payload contains the following attributes:

    {
        "key": "atlassian-connect-addon",
        "clientKey": "1234567890",
        "publicKey": "MIGf....ZRWzwIDAQAB",
        "sharedSecret": "1ad6f705-fe0b-4111-9551-7ce5d81d2884",
        "baseUrl": "http://localhost:2990/jira",
        "productType": "jira",
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

<table class='aui'>
    <thead>
        <tr>
            <th>Attribute</th>
            <th>Description</th>
        </tr>
    </thead>
    <tr>
        <td>`key`</td>
        <td>Add-on key that was installed into the Atlassian Product, as it appears in your add-on's descriptor.</td>
    </tr>
    <tr>
        <td>`clientKey`</td>
        <td>Identifying key for the Atlassian product instance that the add-on was installed into. This will never change for a given
        instance, and is unique across all Atlassian product tenants. This value should be used to key tenant details
        in your add-on.</td>
    </tr>
    <tr>
        <td>`publicKey`</td>
        <td>This is the public key for this Atlassian product instance. You may verify that this `baseUrl` 
			uses this `publicKey` at the standard URL `<host baseUrl>/plugins/servlet/oauth/consumer-info`.</td>
    </tr>
    <tr>
        <td>`sharedSecret`</td>
        <td>Use this string to sign outgoing JWT tokens and validate incoming JWT tokens.</td>
    </tr>
    <tr>
        <td>`baseUrl`</td>
        <td>URL prefix for this Atlassian product instance. All of its REST endpoints begin with this `baseUrl`.</td>
    </tr>
    <tr>
        <td>`productType`</td>
        <td>Identifies the category of Atlassian product, e.g. `jira` or `confluence`.</td>
    </tr>
</table>


### Understanding JWT

<div class="aui-message">
    <p class="title">
        <span class="aui-icon icon-warning"></span>
        <strong>Prerequisite</strong>
    </p>
	<p>
Please make sure you read the [Understanding JWT](understanding-jwt.html) section if you are unfamiliar with JWT.</p></div>

### Making a service Call

The JWT protocol describes the format and verification of individual JWT tokens. However it does not prescribe a method
of transportation. Connect transports JWT tokens as query-string parameters and as authorization headers. 
When communicating server-to-server with the Atlassian host product your add-on must include a JWT token when accessing 
protected resources. This covers most of the REST APIs. Construct a token that identifies your add-on, identifies the query, 
specifies the token's expiry time and allows the receiver to verify that this token was genuinely constructed by your add-on.
You must use one of the following methods to add the JWT token to the API call:

Query string example:

    GET http://localhost:2990/jira/rest/api/2/issue/AC-1.json?jwt=<insert jwt-token here>

Headers example:

    POST http://localhost:2990/jira/rest/api/2/issue/AC-1/attachments
    "Authorization" header value: "JWT <insert jwt-token here>"

For more details on how to create a jwt token, see [Creating a JWT Token](understanding-jwt.html#create).

<a name='exposing'></a>
### Exposing a service

All incoming requests (requests coming from an Atlassian product) should check for the presence of the `jwt` 
query string parameter, which needs to be decoded and verified. In particular, the verification should:

1. Extract the JWT token from the request's `jwt` query parameter or the authorization header.
* Decode the JWT token, without verification. This gives you a header JSON object, a claims JSON object, and a signature.
* Extract the issuer ('iss') claim from the decoded, unverified claims object. This is the `clientKey` for the tenant - 
an identifier for the Atlassian application making the call, which should have been stored by the add-on as part of the 
[installation handshake](#installation).
* Look up the `sharedSecret` for the `clientKey`, as stored by the add-on during the installation handshake
* Verify the signature with the `sharedSecret` and the algorithm specified in the header's `alg` field. 
* Verify the query has not been tampered by [Creating a Query Hash](understanding-jwt.html#qsh) and comparing it against the 
`qsh` claim on the verified token.
* The JWT specification lists some [standard claims](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-13#section-4.1.1) 
that, if present, you should verify. Issuers include these to help you ensure that tokens you receive are used according to the intentions of 
the issuer and with the best possible results.

These steps must be executed before processing the request, and the request must be rejected if any of these steps fail.

For more details on how to decode and validate a JWT token, see [Decoding and Validating a JWT Token](understanding-jwt.html#decode), 
which also provides a comprehensive list of claims supported by Atlassian products that you need to validate.


