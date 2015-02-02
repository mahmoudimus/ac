package it;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Function;
import com.google.gson.Gson;
import it.servlet.InstallHandlerServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jetty.server.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Functional test for add-on properties
 *
 * @since TODO: fill in the proper version before merge
 */
public class TestAddOnProperties extends AbstractBrowserlessTest
{
    public static final int MAX_VALUE_SIZE = 1024 * 32;
    private final static Gson gson = new Gson();

    private final String addOnKey = "testAddOnPropertyAddOnKey";
    private final String restPath = baseUrl + "/rest/atlassian-connect/1/addons/" + addOnKey;

    private ConnectRunner runner = null;
    private InstallHandlerServlet installHandlerServlet;

    @Before
    public void init() throws Exception
    {
        installHandlerServlet = new InstallHandlerServlet();
        runner = new ConnectRunner(baseUrl, addOnKey)
                .addJWT(installHandlerServlet)
                .start();
    }

    @After
    public void after()
    {
        ConnectRunner.stopAndUninstallQuietly(runner);
    }

    @Test
    public void testCreatePropertyWithJWTQueryParameter()
            throws IOException, URISyntaxException, NoSuchAlgorithmException
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        URL url = new URL(restPath + "/properties/" + propertyKey);

        String sharedSecret = checkNotNull(installHandlerServlet.getInstallPayload().getSharedSecret());
        String jwt = AddonTestUtils.generateJwtSignature(HttpMethod.PUT, url.toURI(), addOnKey, sharedSecret, BaseUrlLocator.getBaseUrl(), null);

        URL longerUrl = new URL(restPath + "/properties/" + propertyKey + "?jwt=" + jwt);
        HttpURLConnection connection = (HttpURLConnection) longerUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.getOutputStream().write(propertyKey.getBytes());

        int responseCode = connection.getResponseCode();
        assertEquals(Response.SC_CREATED, responseCode);

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndGetProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "0", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddOnProperty result = gson.fromJson(response, RestAddOnProperty.class);
        assertEquals(property, result);

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndDeleteProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "0", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        assertDeleted(propertyKey);
    }

    @Test
    public void testDeleteNonExistentProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NOT_FOUND, responseCode);
    }

    @Test
    public void testUpdateAndGetProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "0", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, "1").httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        int responseCode2 = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_OK, responseCode2);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddOnProperty result = gson.fromJson(response, RestAddOnProperty.class);
        assertEquals(property, result);

        int responseCode3 = executeDeleteRequest(property.key);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testNoAccessFromSecondPlugin() throws Exception
    {
        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        ConnectRunner secondAddOn = new ConnectRunner(baseUrl, "secondAddOn")
                .addJWT(installHandlerServlet)
                .start();

        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = executePutRequest(propertyKey, "1").httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        assertNotAccessibleGetRequest(propertyKey, Option.option(secondAddOn.getSignedRequestHandler()));

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);

        ConnectRunner.stopAndUninstallQuietly(secondAddOn);
    }

    @Test
    public void testNoAccessWithoutPluginKeyInHeader() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        HttpURLConnection connection = executeGetRequest(propertyKey, Option.<SignedRequestHandler>none());
        assertEquals(Response.SC_UNAUTHORIZED, connection.getResponseCode());
    }

    @Test
    public void testPutValueTooBigReturnsForbidden() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        String tooBigValue = StringUtils.repeat(" ", MAX_VALUE_SIZE + 1);

        int responseCode = executePutRequest(propertyKey, tooBigValue).httpStatusCode;
        assertEquals(Response.SC_FORBIDDEN, responseCode);
    }

    @Test
    public void testGetNotModifiedForSameETag() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        RequestResponse putResponse = executePutRequest(propertyKey, "0");
        HttpURLConnection connection = executeGetRequestWithETag(propertyKey, putResponse.eTag.get());
        assertEquals(Response.SC_NOT_MODIFIED, connection.getResponseCode());

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testSetNotModifiedForDifferentETag() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        executePutRequest(propertyKey, "0");
        RequestResponse putResponse = executePutRequest(propertyKey, "1", Option.some("\"a\""));
        assertEquals(Response.SC_PRECONDITION_FAILED, putResponse.httpStatusCode);

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testSetModifiedForEmptyETag() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        RequestResponse putResponse = executePutRequest(propertyKey, "1", Option.some("\"\""));
        assertEquals(Response.SC_CREATED, putResponse.httpStatusCode);

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testSetNotModifiedForDifferentEmptyETag() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        executePutRequest(propertyKey, "0");
        RequestResponse putResponse = executePutRequest(propertyKey, "1", Option.some("\"\""));
        assertEquals(Response.SC_PRECONDITION_FAILED, putResponse.httpStatusCode);

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testMaximumPropertiesNotReached() throws Exception
    {
        String propertyKeyPrefix = RandomStringUtils.randomAlphanumeric(15);
        for (int i = 0; i < 50; i++)
        {
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), "3").httpStatusCode;
            assertEquals(Response.SC_CREATED, responseCode);
        }
        for (int i = 0; i < 50; i++)
        {
            assertDeleted(propertyKeyPrefix + String.valueOf(i));
        }
    }

    @Test
    public void testMaximumPropertiesReached() throws Exception
    {
        String propertyKeyPrefix = RandomStringUtils.randomAlphanumeric(15);
        for (int i = 0; i < 50; i++)
        {
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), "3").httpStatusCode;
            assertEquals(Response.SC_CREATED, responseCode);
        }

        int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(51), "3").httpStatusCode;
        assertEquals(Response.SC_CONFLICT, responseCode);

        for (int i = 0; i < 50; i++)
        {
            assertDeleted(propertyKeyPrefix + String.valueOf(i));
        }
    }

    @Test
    public void testSuccessfulListRequest() throws IOException, URISyntaxException
    {
        // should clean all properties before running, else this test depends on previous test failures!
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "1", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyList();
        RestAddOnPropertiesBean result = gson.fromJson(response, RestAddOnPropertiesBean.class);

        RestAddOnPropertiesBean expected = RestAddOnPropertiesBean.fromRestAddOnProperties(property);
        assertEquals(expected, result);
        assertDeleted(propertyKey);
    }

    private String getSelfForPropertyKey(final String propertyKey)
    {
        return restPath + "/properties/" + propertyKey;
    }

    private void assertDeleted(String propertyKey) throws IOException, URISyntaxException
    {
        int responseCode = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode);
    }

    private void assertNotAccessibleGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, signedRequestHandler);
        assertEquals(Response.SC_NOT_FOUND, connection.getResponseCode());
    }

    private String sendSuccessfulGetRequestForPropertyKey(final String propertyKey)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, Option.option(runner.getSignedRequestHandler()));
        assertEquals(Response.SC_OK, connection.getResponseCode());
        return IOUtils.toString(connection.getInputStream());
    }

    private String sendSuccessfulGetRequestForPropertyList() throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        runner.getSignedRequestHandler().sign(url.toURI(), "GET", null, connection);

        assertEquals(Response.SC_OK, connection.getResponseCode());

        return IOUtils.toString(connection.getInputStream());
    }

    private HttpURLConnection executeGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        if (signedRequestHandler.isDefined())
        {
            signedRequestHandler.get().sign(url.toURI(), "GET", null, connection);
        }
        return connection;
    }

    private HttpURLConnection executeGetRequestWithETag(final String propertyKey, final String eTag)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, Option.option(runner.getSignedRequestHandler()));
        connection.setRequestProperty("If-None-Match", eTag);
        return connection;
    }

    private RequestResponse executePutRequest(final String propertyKey, String value) throws IOException, URISyntaxException
    {
        return executePutRequest(propertyKey, value, Option.<String>none());
    }

    private RequestResponse executePutRequest(final String propertyKey, String value, Option<String> eTag) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        if (eTag.isDefined())
        {
            connection.setRequestProperty("If-Match", eTag.get());
        }
        runner.getSignedRequestHandler().sign(url.toURI(), "PUT", null, connection);
        connection.setDoOutput(true);
        connection.getOutputStream().write(value.getBytes());

        Option<String> returnedETag = Option.option(connection.getHeaderField("ETag")).map(new Function<String, String>()
        {
            @Override
            public String apply(final String input)
            {
                return input;
            }
        });
        return new RequestResponse(connection.getResponseCode(), returnedETag);
    }

    private int executeDeleteRequest(final String propertyKey) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties/" + propertyKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        runner.getSignedRequestHandler().sign(url.toURI(), "DELETE", null, connection);
        return connection.getResponseCode();
    }

    private class RequestResponse
    {
        private final int httpStatusCode;
        private final Option<String> eTag;

        private RequestResponse(final int httpStatusCode, final Option<String> eTag) {
            this.httpStatusCode = httpStatusCode;
            this.eTag = eTag;
        }
    }

    private class RestAddOnProperty
    {
        @JsonProperty
        private final String key;
        @JsonProperty
        private final String value;
        @JsonProperty
        private final String self;

        public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final String value, @JsonProperty ("self") final String self)
        {
            this.key = key;
            this.value = value;
            this.self = self;
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(key)
                    .append(value)
                    .append(self).toHashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final RestAddOnProperty other = (RestAddOnProperty) obj;
            return new EqualsBuilder()
                    .append(key, other.key)
                    .append(value, other.value)
                    .append(self, other.self).isEquals();
        }

        @Override
        public String toString()
        {
            return "RestAddonProperty{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", self='" + self + '\'' +
                    '}';
        }
    }

    private static class RestAddOnPropertiesBean
    {
        @JsonProperty
        private RestAddOnPropertyBean[] keys;

        public RestAddOnPropertiesBean(@JsonProperty RestAddOnPropertyBean[] keys)
        {
            this.keys = keys;
        }

        @Override
        public int hashCode() { return new HashCodeBuilder().append(this.keys).toHashCode();}

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final RestAddOnPropertiesBean other = (RestAddOnPropertiesBean) obj;
            return new EqualsBuilder().append(this.keys, other.keys).isEquals();
        }

        @Override
        public String toString()
        {
            return "RestAddOnPropertiesBean{" +
                    "properties=" + keys +
                    '}';
        }

        static RestAddOnPropertiesBean fromRestAddOnProperties(RestAddOnProperty... properties)
        {
            RestAddOnPropertyBean[] propertyBeans = new RestAddOnPropertyBean[properties.length];
            for (int i = 0; i < properties.length; i++)
            {
                propertyBeans[i] = new RestAddOnPropertyBean(properties[i].self, properties[i].key);
            }
            return new RestAddOnPropertiesBean(propertyBeans);
        }

        public static class RestAddOnPropertyBean
        {
            @JsonProperty
            private final String self;
            @JsonProperty
            private final String key;

            public RestAddOnPropertyBean(@JsonProperty final String self, @JsonProperty final String key)
            {
                this.self = self;
                this.key = key;
            }

            @Override
            public int hashCode() { return new HashCodeBuilder().append(this.self).append(this.key).toHashCode();}

            @Override
            public boolean equals(final Object obj)
            {
                if (obj == null) {return false;}
                if (getClass() != obj.getClass()) {return false;}
                final RestAddOnPropertyBean other = (RestAddOnPropertyBean) obj;
                return new EqualsBuilder().append(this.self, other.self).append(this.key, other.key).isEquals();
            }

            @Override
            public String toString()
            {
                return "RestAddOnPropertyBean{" +
                        "self='" + self + '\'' +
                        ", key='" + key + '\'' +
                        '}';
            }
        }
    }
}
