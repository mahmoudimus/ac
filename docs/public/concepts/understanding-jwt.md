# Understanding JWT

There is a [nicely presented copy](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html) of the specification. 
JSON Web Token (JWT) is a compact URL-safe means of representing claims to be transferred between two parties. 
The claims in a JWT are encoded as a JavaScript Object Notation (JSON) object that is used as the payload of a 
JSON Web Signature (JWS) structure or as the plaintext of a JSON Web Encryption (JWE) structure, enabling the claims to 
be digitally signed or MACed and/or encrypted.

## Structure of a JWT Token

A JWT token looks like this: 

	eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEzODY4OTkxMzEsImlzcyI6ImppcmE6MTU0ODk1OTUiLCJxc2giOiI4MDYzZmY0Y2ExZTQxZGY3YmM5MGM4YWI2ZDBmNjIwN2Q0OTFjZjZkYWQ3YzY2ZWE3OTdiNDYxNGI3MTkyMmU5IiwiaWF0IjoxMzg2ODk4OTUxfQ.uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo

Once you understand the format, it's actually pretty simple: 

	<base64-encoded header>.<base64-encoded claims>.<base64-encoded signature>

In other words:

* You create a header object, with the JSON format. Then you encode it as a base64
* You create a claims object, with the JSON format. Then you encode it in base64
* You create a signature for the URI (we'll get into that later). Then you encode it in base64
* You concatenate the three items, with the "." separator

You shouldn't actually have to do this manually, as there are libraries available in most languages, as we describe in the [JWT libraries](#jwtlib) section. 
However it is important you understand the fields in the JSON header and claims objects described in the next sections:

### Header

The JWT Header declares that the encoded object is a JSON Web Token (JWT) and the JWT is a JWS that is MACed using the HMAC SHA-256 algorithm. For example:

	{
		"typ":"JWT",
		"alg":"HS256"
	}

<table class='aui'>
	<thead>
        <tr>
            <th>Attribute</th>
			<th>Type</th>
            <th>Description</th>
        </tr>
    </thead>
    <tr>
        <td>"typ" (mandatory)</td><td>String</td><td>Type for the token, defaulted to "JWT". Specifies that this is a JWT token</td>
	</tr>
	<tr>
		<td>"alg" (mandatory)</td><td>String</td><td>Algorithm. specifies the algorithm used to sign the token. 
			In atlassian-connect version 1.0 we support the HMAC SHA-256 algorithm, which the 
			[JWT specification](http://tools.ietf.org/html/draft-ietf-oauth-json-web-token-13) identifies using the string ["HS256"](http://tools.ietf.org/html/draft-ietf-jose-json-web-algorithms-18#section-3.1).</td>
	</tr>
</table>

<a name='claims'></a>
### Claims

The JWT claims object contains security information about the message. For example:

	{
		"iss": "jira:1314039",
		"iat": 1300819370,
		"exp": 1300819380,
		"qsh": "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9",
		"sub": "a_user_key"
	}

<table class='aui'>
    <thead>
        <tr>
            <th>Attribute</th>
			<th>Type</th>
            <th>Description</th>
        </tr>
    </thead>
    <tr>
        <td>`iss` (mandatory)</td>
		<td>String</td>
        <td>the issuer of the claim. Connect uses it to identify the application making the call. for example: 

 * If the Atlassian product is the calling application: contains the unique identifier of the tenant. 
 This is the `clientKey` that you receive in the `installed` callback. You should reject unrecognised issuers.
 * If the add-on is the calling application: the add-on key specified in the add-on descriptor</td>
    </tr>
    <tr>
        <td>`iat` (mandatory)</td>
		<td>Long</td>
        <td>Issued-at time. Contains the UTC Unix time at which this token was issued. There are no hard 
			requirements around this claim but it does not make sense for it to be significantly in the future. 
			Also, significantly old issued-at times may indicate the replay of suspiciously old tokens. </td>
    </tr>
    <tr>
        <td>`exp` (mandatory)</td>
		<td>Long</td>
        <td>Expiration time. It contains the UTC Unix time after which you should no longer accept this token. 
			It should be after the issued-at time.</td>
    </tr>
    <tr>
        <td>`qsh` (mandatory)</td>
		<td>String</td>
        <td>query hash. A custom Atlassian claim that prevents URL tampering.</td>
    </tr>
    <tr>
        <td>`sub` (optional)</td>
		<td>String</td>
        <td>The subject of this token. In atlassian-connect pre 1.0 we use this to identify the user key 
			of the user that the add-on wishes to impersonate with this call; from 1.0 onwards this claim 
			will be ignored, as user impersonation will not be possible.</td>
    </tr>

</table>

You should use a little leeway when processing time-based claims, as clocks may drift apart. 
The JWT specification suggests no more than a few minutes.
Judicious use of the time-based claims allows for replays within a limited window. 
This can be useful when all or part of a page is refreshed or when it is valid for a user to 
repeatedly perform identical actions (e.g. clicking the same button).

<a name="jwtlib"></a>
## JWT Libraries

Most modern languages have JWT libraries available. We recommend you use one of these libraries 
(or other JWT-compatible libraries) before trying to hand-craft the JWT token.

<table class='aui'>
    <thead>
        <tr>
            <th>Language</th>
			<th>Library</th>
        </tr>
    </thead>
	<tr><td>Java</td><td>[atlassian-jwt](https://bitbucket.org/atlassian/atlassian-jwt/) and [jsontoken](https://code.google.com/p/jsontoken/)</td></tr>
	<tr><td>Node.js</td><td>[node-jwt-simple](https://github.com/hokaccha/node-jwt-simple)</td></tr>
	<tr><td>Ruby</td><td>[ruby-jwt](https://github.com/progrium/ruby-jwt)</td></tr>
	<tr><td>PHP</td><td>[firebase php-jwt](https://github.com/firebase/php-jwt) and [luciferous jwt](https://github.com/luciferous/jwt)</td></tr>
	<tr><td>.NET</td><td>[jwt](https://github.com/johnsheehan/jwt)</td></tr>
    <tr><td>Haskell</td><td>[haskell-jwt](http://hackage.haskell.org/package/jwt)</td></tr>
</table>

The [JWT decoder](http://jwt-decoder.herokuapp.com/jwt/decode) is a handy web based decoder for Atlassian Connect JWT tokens.

<a name='create'></a>
## Creating a JWT Token 

Here is an example of creating a JWT token, in Java, using atlassian-jwt and nimbus-jwt:

<pre><code data-lang="java">
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import com.atlassian.jwt.*;
import com.atlassian.jwt.core.writer.*;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriterFactory;

public class JWTSample {
	
	public String createUriWithJwt() 
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		long issuedAt = System.currentTimeMillis() / 1000L;
		long expiresAt = issuedAt + 180L; 
		String key = "atlassian-connect-addon"; //the key from the add-on descriptor    
		String sharedSecret = "..."; 	//the sharedsecret key received 
										//during the addon installation handshake
		String method = "GET";
		String baseUrl = "http://localhost:2990/jira";
		String contextPath = "/jira";
		String apiPath = "/rest/api/latest/serverInfo";

		JwtJsonBuilder jwtBuilder = new JsonSmartJwtJsonBuilder()
		        .issuedAt(issuedAt)
		        .expirationTime(expiresAt)
		        .issuer(key);

		CanonicalHttpUriRequest canonical = new CanonicalHttpUriRequest(method,
		        apiPath, contextPath, new HashMap());
		JwtClaimsBuilder.appendHttpRequestClaims(jwtBuilder, canonical);

		JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
		String jwtbuilt = jwtBuilder.build();
		String jwtToken = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256,
		        sharedSecret).jsonToJwt(jwtbuilt);

		String apiUrl = baseUrl + apiPath + "?jwt=" + jwtToken;
		return apiUrl;
	}
}</code></pre>

<a name='decode'></a>
## Decoding and Verifying a JWT Token 

### Decoding a JWT Token

Decoding the JWT token reverses the steps followed during the creation of the token, 
to extract the header, claims and signature. Here is an example in Java:

<pre><code data-lang="java">
String jwtToken = ...;//e.g. extracted from the request
String[] base64EncodedSegments = jwtToken.split('.');
String base64EncodedHeader = base64EncodedSegments[0];
String base64EncodedClaims = base64EncodedSegments[1];
String signature = base64EncodedSegments[2];
String header = base64decode(base64EncodedHeader);
String claims = base64decode(base64EncodedClaims);
</code></pre>

This gives us the following:

Header:

	{
		"alg": "HS256",
		"typ": "JWT"
	}

Claims:

    {
        "iss": "jira:15489595",
        "iat": 1386898951,
        "qsh": "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9",
        "exp": 1386899131
    }

Signature:

    uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo

<a name='verify'></a>
### Verifying a JWT token

JWT libraries typically provide methods to be able to verify a received JWT token. 
Here is an example using nimbus-jose-jwt and json-smart:

<pre><code data-lang="java">import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;

public JWTClaimsSet read(String jwt, JWSVerifier verifier) throws ParseException, JOSEException
{
    JWSObject jwsObject = JWSObject.parse(jwt);

    if (!jwsObject.verify(verifier))
    {
        throw new IllegalArgumentException("Fraudulent JWT token: " + jwt);
    }

    JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
    return JWTClaimsSet.parse(jsonPayload);
}
</code></pre>

<a name='qsh'></a>
## Creating a Query Hash

A query string hash is a signed canonical request for the URI of the API you want to call. 

	qsh = `sign(canonical-request)`
	canonical-request = `canonical-method + '&' + canonical-URI + '&' + canonical-query-string` 

A canonical request is a normalised representation of the URI. Here is an example. For the following URL, 
assuming you want to do a "GET" operation:

        "http://localhost:2990/path/to/service?zee_last=param&repeated=parameter 1&first=param&repeated=parameter 2"

The canonical request is

        "GET&/path/to/service&first=param&repeated=parameter%201,parameter%202&zee_last=param"

To create a query string hash, follow the detailed instructions below:

1. Compute canonical method
  *  Simply the upper-case of the method name (e.g. `"GET"` or `"PUT"`)<br><br>
2. Append the character `'&'`<br><br>
3. Compute canonical URI
  *  Discard the protocol, server, port, context path and query parameters from the full URL.
     *  For requests targeting add-ons discard the `baseUrl` in the add-on descriptor.
  *  Removing the context path allows a reverse proxy to redirect incoming requests for `"jira.example.com/getsomething"`
   to `"example.com/jira/getsomething"` without breaking authentication. The requester cannot know that the reverse proxy
   will prepend the context path `"/jira"` to the originally requested path `"/getsomething"`
  *  Empty-string is not permitted; use `"/"` instead.
  *  Url-encode any `'&'` characters in the path.
  *  Do not suffix with a `'/'` character unless it is the only character. e.g.
     *  Canonical URI of `"http://localhost:2990/jira/some/path/?param=value"` is `"/some/path"`
     *  Canonical URI of `"http://localhost:2990"` is `"/"`
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

6. Convert the canonical request string to bytes
   *  The encoding used to represent characters as bytes is `UTF-8`<br><br>
7. Hash the canonical request bytes using the `SHA-256` algorithm
   * e.g. The `SHA-256` hash of `"foo"` is `"2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae

## Advanced: Creating a JWT Token Manually

<div class="aui-message warning">
	    <p class="title">
	        <span class="aui-icon icon-warning"></span>
	        <strong>Disclaimer</strong>
	    </p>
	    You should only need to read this section if you are planning to create JWT tokens manually, 
		i.e. if you are not using one of the libraries listed in the previous section
	</div>
	
### More details on JWT Tokens

The format of a JWT token is simple: ```<base64-encoded header>.<base64-encoded claims>.<signature>```.

* Each section is separated from the others by a period character (```.```).
* Each section is base-64 encoded, so you will need to decode each one to make them human-readable. 
* The header specifies a very small amount of information that the receiver needs in order to parse and verify the JWT token.
 * All JWT token headers state that the type is "JWT".
 * The algorithm used to sign the JWT token is needed so that the receiver can verify the signature.
* The claims are a list of assertions that the issuer is making: each says that "this named field" has "this value".
 * Some, like the "iss" claim, which identifies the issuer of this JWT token, have standard names and uses.
 * Others are custom claims. We limit our use of custom claims as much as possible, for ease of implementation.
* The signature is computed by using an algorithm such as HMAC SHA-256 plus the header and claims sections.
 * The receiver verifies that the signature must have been computed using the genuine JWT header and claims sections, 
 the indicated algorithm and a previously established secret.
 * An attacker tampering with the header or claims will cause signature verification to fail.
 * An attacker signing with a different secret will cause signature verification to fail.
 * There are various algorithm choices legal in the JWT spec. In atlassian-connect version 1.0 we support HMAC SHA-256.
 
 
 ### Steps to Follow
 
 1. Create a header JSON object
 * Convert the header JSON object to a UTF-8 encoded string and base-64 encode it. That gives you encodedHeader.
 * Create a claims JSON object, including a [query string hash](#qsh)
 * Convert the claims JSON object to a UTF-8 encoded string and base-64 encode it. That gives you encodedClaims.
 * Concatenate the encoded header, a period character (```.```) and the encoded claims set. That gives you signingInput = encodedHeader+ "." + encodedClaims. 
 * Compute the signature of signingInput using the JWT or cryptographic library of your choice. Then base64 encode it. That gives you encodedSignature.
 * concatenate the signing input, another period character and the signature, which gives you the JWT token. jwtToken = signingInput + "." + encodedSignature
 
 
 ### Example

 Here is an example in Java using gson, commons-codec, and the Java security and crypto libraries:

 <pre><code data-lang="java">
public class JwtClaims {
 	protected String iss;
 	protected long iat;
 	protected long exp;
 	protected String qsh;
 	protected String sub;
	// + getters/setters/constructors
}
	 
[...]
	 
public class JwtHeader {
	protected String alg;
	protected String typ;
	 // + getters/setters/constructors
}
	 
[...]

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import java.io.UnsupportedEncodingException;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import com.google.gson.Gson;

public class JwtBuilder {

    public static String generateJWTToken(String requestUrl, String canonicalUrl, 
        String key, String sharedSecret) 
        		 throws NoSuchAlgorithmException, UnsupportedEncodingException, 
        		 InvalidKeyException {

        JwtClaims claims = new JwtClaims();
        claims.setIss(key);
        claims.setIat(System.currentTimeMillis() / 1000L);
        claims.setExp(claims.getIat() + 180L);

        claims.setQsh(getQueryStringHash(canonicalUrl));
        String jwtToken = sign(claims, sharedSecret);
        return jwtToken;
    }

    private static String sign(JwtClaims claims, String sharedSecret) 
            throws InvalidKeyException, NoSuchAlgorithmException {
         String signingInput = getSigningInput(claims, sharedSecret);
         String signed256 = signHmac256(signingInput, sharedSecret);
         return signingInput + "." + signed256;
     }

     private static String getSigningInput(JwtClaims claims, String sharedSecret) 
            throws InvalidKeyException, NoSuchAlgorithmException {
         JwtHeader header = new JwtHeader();
         header.alg = "HS256";
         header.typ = "JWT";
         Gson gson = new Gson();
         String headerJsonString = gson.toJson(header);
         String claimsJsonString = gson.toJson(claims);
         String signingInput = encodeBase64URLSafeString(headerJsonString
                 .getBytes())
                 + "."
                 + encodeBase64URLSafeString(claimsJsonString.getBytes());
         return signingInput;
     }

     private static String signHmac256(String signingInput, String sharedSecret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey key = new SecretKeySpec(sharedSecret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return encodeBase64URLSafeString(mac.doFinal(signingInput.getBytes()));
    }     

    private static String getQueryStringHash(String canonicalUrl) 
            throws NoSuchAlgorithmException,UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(canonicalUrl.getBytes("UTF-8"));
         byte[] digest = md.digest();
         return encodeHexString(digest);
     }
 }
		
[...]
		
public class Sample {
	public String getUrlSample() throws Exception {
    	String requestUrl = 
			"http://localhost:2990/jira/rest/atlassian-connect/latest/license";
		String canonicalUrl = "GET&/rest/atlassian-connect/latest/license&";
		String key = "..."; 	//from the add-on descriptor 
							//and received during installation handshake
		String sharedSecret = "..."; //received during installation Handshake
							
		String jwtToken = JwtBuilder.generateJWTToken(
			requestUrl, canonicalUrl, key, sharedSecret);
		String restAPIUrl = requestUrl + "?jwt=" + jwtToken;
		return restAPIUrl;
	}
} 
 </code></pre>
