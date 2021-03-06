package it.com.atlassian.plugin.connect.util.request;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.Response.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RequestUtil {
    private final ApplicationProperties applicationProperties;

    public RequestUtil(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Request.Builder requestBuilder() {
        return new Request.Builder(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE));
    }

    public String getApplicationRestUrl(String path) {
        return applicationProperties.getBaseUrl(UrlMode.ABSOLUTE) + "/rest" + path;
    }

    public Response makeRequest(Request request) throws IOException {
        URL url = request.getUrl();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(request.getMethod().toString());

        if (request.hasBasicAuth()) {
            connection.setRequestProperty("Authorization", "Basic " + request.getBasicAuthHeaderValue());
        }

        if (request.isJson()) {
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
        }

        connection.connect();

        try {
            int responseCode = connection.getResponseCode();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            StringBuilder output = new StringBuilder();

            try (
                    InputStream response = isResponseSuccessful(responseCode) ? connection.getInputStream() : connection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"))
            ) {
                for (String line; (line = reader.readLine()) != null; ) {
                    output.append(line).append('\n');
                }
            }

            return new Response(responseCode, headerFields, output.toString());
        } finally {
            connection.disconnect();
        }
    }

    private boolean isResponseSuccessful(final int responseCode) {
        return Status.fromStatusCode(responseCode).getFamily() == Status.Family.SUCCESSFUL;
    }

    public static class Request {
        private HttpMethod method;
        private URI uri;
        private String username;
        private String password;
        private boolean isJson;

        private Request(final HttpMethod method, final URI uri, final String username, final String password, final boolean isJson) {
            this.method = method;
            this.uri = uri;
            this.username = username;
            this.password = password;
            this.isJson = isJson;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public URI getUri() {
            return uri;
        }

        public URL getUrl() throws MalformedURLException {
            return uri.toURL();
        }

        public boolean hasBasicAuth() {
            return StringUtils.isNotBlank(username);
        }

        public boolean isJson() {
            return isJson;
        }

        public String getBasicAuthHeaderValue() {
            String auth = username + ":" + password;
            return new String(Base64.encodeBase64(auth.getBytes()));
        }

        public static class Builder {
            private HttpMethod method;
            private URI uri;
            private String username;
            private String password;
            private boolean isJson = true;
            private boolean includeJwtAuthentication = false;
            private String applicationBaseUrl;
            private String addonKey;
            private String addonSecret;

            private Builder(String applicationBaseUrl) {
                this.applicationBaseUrl = applicationBaseUrl;
            }

            public Builder setMethod(final HttpMethod method) {
                this.method = method;
                return this;
            }

            public Builder setUri(final URI uri) {
                this.uri = uri;
                return this;
            }

            public Builder setUri(final String uri) {
                this.uri = URI.create(uri);
                return this;
            }

            public Builder setUsername(final String username) {
                this.username = username;
                return this;
            }

            public Builder setPassword(final String password) {
                this.password = password;
                return this;
            }

            public Builder setJson(boolean isJson) {
                this.isJson = isJson;
                return this;
            }

            public Builder setIncludeJwtAuthentication(String addonKey, String addonSecret) {
                this.includeJwtAuthentication = true;
                this.addonKey = addonKey;
                this.addonSecret = addonSecret;
                return this;
            }

            public Request build() {
                if (this.includeJwtAuthentication) {
                    appendJwtToUri();
                }
                return new Request(method, uri, username, password, isJson);
            }

            private void appendJwtToUri() {
                String queryHash;
                try {
                    queryHash = HttpRequestCanonicalizer.computeCanonicalRequestHash(
                            new CanonicalHttpUriRequest(method.name(), uri.getPath(),
                                    URI.create(applicationBaseUrl).getPath()));
                } catch (Exception e) {
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

    public static class Response {
        private final int statusCode;
        private Map<String, List<String>> headerFields;
        private final String body;

        public Response(int statusCode, Map<String, List<String>> headerFields, String body) {
            this.statusCode = statusCode;
            this.headerFields = headerFields;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public Map<String, List<String>> getHeaderFields() {
            return headerFields;
        }

        public String getBody() {
            return body;
        }

        public Map getJsonBody() throws IOException {
            return new ObjectMapper().readValue(body, Map.class);
        }

        public <T> T getJsonBody(Class<T> bodyClass) throws IOException {
            return new ObjectMapper().readValue(body, bodyClass);
        }
    }
}
