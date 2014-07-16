package it.com.atlassian.plugin.connect.util;

import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class RequestUtil
{
    private final ApplicationProperties applicationProperties;

    public RequestUtil(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public Request.Builder requestBuilder()
    {
        return new Request.Builder();
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

            public Request build()
            {
                return new Request(method, uri, username, password, isJson);
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
    }
}
