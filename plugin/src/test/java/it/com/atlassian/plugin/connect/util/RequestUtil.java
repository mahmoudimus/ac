package it.com.atlassian.plugin.connect.util;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.google.gson.Gson;
import it.com.atlassian.plugin.connect.TestConstants;
import net.oauth.*;
import net.oauth.signature.OAuthSignatureMethod;
import net.oauth.signature.RSA_SHA1;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil
{
    private final ApplicationProperties applicationProperties;

    public RequestUtil(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public Request constructOAuthRequestFromAddOn(String addOnKey) throws IOException, OAuthException, URISyntaxException
    {
        final HttpMethod httpMethod = HttpMethod.GET;
        URI uri = URI.create(getApplicationRestUrl("/applinks/1.0/manifest"));
        uri = signOAuthUri(httpMethod, uri, addOnKey);

        return requestBuilder()
                .setMethod(httpMethod)
                .setUri(uri)
                .build();
    }

    private static URI signOAuthUri(HttpMethod httpMethod, URI uri, String addOnKey) throws IOException, OAuthException, URISyntaxException
    {
        final Map<String, String> oAuthParams = new HashMap<String, String>();
        {
            oAuthParams.put(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
            oAuthParams.put(OAuth.OAUTH_VERSION, "1.0");
            oAuthParams.put(OAuth.OAUTH_CONSUMER_KEY, addOnKey);
            oAuthParams.put(OAuth.OAUTH_NONCE, String.valueOf(System.nanoTime()));
            oAuthParams.put(OAuth.OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        }
        final OAuthMessage oAuthMessage = new OAuthMessage(httpMethod.toString(), uri.toString(), oAuthParams.entrySet());
        final OAuthConsumer oAuthConsumer = new OAuthConsumer(null, addOnKey, TestConstants.XML_ADDON_PRIVATE_KEY, new OAuthServiceProvider(null, null, null));
        oAuthConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, TestConstants.XML_ADDON_PRIVATE_KEY);
        final OAuthSignatureMethod oAuthSignatureMethod = OAuthSignatureMethod.newSigner(oAuthMessage, new OAuthAccessor(oAuthConsumer));
        oAuthSignatureMethod.sign(oAuthMessage);
        return addOAuthParamsToRequest(uri, oAuthMessage);
    }

    private static URI addOAuthParamsToRequest(URI uri, OAuthMessage oAuthMessage) throws IOException
    {
        StringBuilder sb = new StringBuilder("?");
        {
            boolean isFirst = true;

            for (Map.Entry<String, String> entry : oAuthMessage.getParameters())
            {
                if (!isFirst)
                {
                    sb.append('&');
                }

                isFirst = false;
                sb.append(entry.getKey()).append('=').append(JwtUtil.percentEncode(entry.getValue())); // for JWT use the same encoding as OAuth 1
            }

            uri = URI.create(uri + sb.toString());
        }
        return uri;
    }

    public Request.Builder requestBuilder()
    {
        return new Request.Builder(this.applicationProperties.getBaseUrl(UrlMode.ABSOLUTE));
    }

    public String getApplicationRestUrl(String path)
    {
        return applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + "/rest" + path;
    }

    public Response makeRequest(Request request) throws IOException
    {
        URL url = request.getUrl();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request.getMethod().toString());

        if (request.hasBasicAuth())
        {
            connection.setRequestProperty("Authorization", "Basic " + request.getBasicAuthHeaderValue());
        }

        if (request.isJson())
        {
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
        }

        connection.connect();

        try
        {
            int responseCode = connection.getResponseCode();
            StringBuilder output = new StringBuilder();

            InputStream response = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            try
            {
                for (String line; (line = reader.readLine()) != null;)
                {
                    output.append(line).append('\n');
                }
            }
            finally
            {
                reader.close();
                response.close();
            }
            return new Response(responseCode, output.toString());
        }
        finally
        {
            connection.disconnect();
        }
    }

    public static class Request
    {
        private HttpMethod method;
        private URI uri;
        private String username;
        private String password;
        private boolean isJson;

        private Request(final HttpMethod method, final URI uri, final String username, final String password, final boolean isJson)
        {
            this.method = method;
            this.uri = uri;
            this.username = username;
            this.password = password;
            this.isJson = isJson;
        }

        public HttpMethod getMethod()
        {
            return method;
        }

        public URI getUri()
        {
            return uri;
        }

        public URL getUrl() throws MalformedURLException
        {
            return uri.toURL();
        }

        public boolean hasBasicAuth()
        {
            return StringUtils.isNotBlank(username);
        }

        public boolean isJson()
        {
            return isJson;
        }

        public String getBasicAuthHeaderValue()
        {
            String auth = username + ":" + password;
            return new String(Base64.encodeBase64(auth.getBytes()));
        }

        public static class Builder
        {
            private HttpMethod method;
            private URI uri;
            private String username;
            private String password;
            private boolean isJson = true;
            private boolean includeJwtAuthentication = false;
            private String applicationBaseUrl;
            private String addonKey;
            private String addonSecret;

            private Builder(String applicationBaseUrl)
            {
                this.applicationBaseUrl = applicationBaseUrl;
            }

            public Builder setMethod(final HttpMethod method)
            {
                this.method = method;
                return this;
            }

            public Builder setUri(final URI uri)
            {
                this.uri = uri;
                return this;
            }

            public Builder setUri(final String uri)
            {
                this.uri = URI.create(uri);
                return this;
            }

            public Builder setUsername(final String username)
            {
                this.username = username;
                return this;
            }

            public Builder setPassword(final String password)
            {
                this.password = password;
                return this;
            }

            public Builder setJson(boolean isJson)
            {
                this.isJson = isJson;
                return this;
            }

            public Builder setIncludeJwtAuthentication(String addonKey, String addonSecret)
            {
                this.includeJwtAuthentication = true;
                this.addonKey = addonKey;
                this.addonSecret = addonSecret;
                return this;
            }

            public Request build()
            {
                if (this.includeJwtAuthentication) {
                    this.appendJwtToUri();
                }
                return new Request(method, uri, username, password, isJson);
            }

            private void appendJwtToUri()
            {
                String queryHash;
                try
                {
                    queryHash = HttpRequestCanonicalizer.computeCanonicalRequestHash(
                            new CanonicalHttpUriRequest(this.method.name(), this.uri.getPath(),
                                    URI.create(this.applicationBaseUrl).getPath()));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }

                JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
                JwtWriter jwtWriter = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, addonSecret);
                JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder().issuer(addonKey).queryHash(queryHash);
                String jwtToken = jwtWriter.jsonToJwt(jsonBuilder.build());
                this.uri = URI.create(uri.toString() + "?jwt=" + jwtToken);
            }
        }
    }

    public static class Response
    {
        private final int statusCode;
        private final String body;

        public Response(int statusCode, String body)
        {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode()
        {
            return statusCode;
        }

        public String getBody()
        {
            return body;
        }

        public Map getJsonBody()
        {
            Gson gson = new Gson();
            return gson.fromJson(body, Map.class);
        }

        public <T> T getJsonBody(Class<T> bodyClass) {
            return new Gson().fromJson(this.body, bodyClass);
        }

        public <T> T getJsonBody(Type bodyType) {
            return new Gson().fromJson(this.body, bodyType);
        }
    }
}
