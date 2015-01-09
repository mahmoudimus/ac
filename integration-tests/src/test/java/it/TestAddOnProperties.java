package it;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
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

import static org.junit.Assert.assertEquals;

/**
 * Functional test for add-on properties
 *
 * @since TODO: fill in the proper version before merge
 */
public class TestAddOnProperties extends AbstractBrowserlessTest
{
    public static final int MAX_VALUE_SIZE = 1024 * 32;
    final String addOnKey = "testAddOnPropertyAddOnKey";
    final String restPath = baseUrl + "/rest/atlassian-connect/1/addons/" + addOnKey;
    final static Gson gson = new Gson();

    ConnectRunner runner = null;

    @Before
    public void init() throws Exception
    {
        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
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
    public void testCreateAndGetProperty() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "TEST_VALUE", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequest(property.key);
        RestAddOnProperty result = gson.fromJson(response, RestAddOnProperty.class);
        assertEquals(property, result);

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndDeleteProperty() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "TEST_VALUE", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        assertDeleted(propertyKey);
    }

    @Test
    public void testDeleteNonExistentProperty() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NOT_FOUND, responseCode);
    }

    private void assertDeleted(String propertyKey) throws IOException, URISyntaxException
    {
        int responseCode = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode);
    }

    private String getSelfForPropertyKey(final String propertyKey)
    {
        return restPath + "/properties/" + propertyKey;
    }

    @Test
    public void testUpdateAndGetProperty() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "TEST_VALUE", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, "TEST_VALUE_2").httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        int responseCode2 = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_OK, responseCode2);

        String response = sendSuccessfulGetRequest(property.key);
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

        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = executePutRequest(propertyKey, "TEST_VALUE_2").httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        assertNotAccessibleGetRequest(propertyKey, Option.option(secondAddOn.getSignedRequestHandler()));

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);

        ConnectRunner.stopAndUninstallQuietly(secondAddOn);
    }

    @Test
    public void testNoAccessWithoutPluginKeyInHeader() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        HttpURLConnection connection = executeGetRequest(propertyKey, Option.<SignedRequestHandler>none());
        assertEquals(Response.SC_UNAUTHORIZED, connection.getResponseCode());
    }

    @Test
    public void testPutValueTooBigReturnsForbidden() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        final String tooBigValue = StringUtils.repeat(" ", MAX_VALUE_SIZE + 1);

        int responseCode = executePutRequest(propertyKey, tooBigValue).httpStatusCode;
        assertEquals(Response.SC_FORBIDDEN, responseCode);
    }

    @Test
    public void testGetNotModifiedForSameETag() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        RequestResponse putResponse = executePutRequest(propertyKey, "TEST_VALUE");
        HttpURLConnection connection = executeGetRequest(propertyKey, Option.option(runner.getSignedRequestHandler()));
        connection.setRequestProperty("If-Match", putResponse.eTag.get());
        assertEquals(Response.SC_NOT_MODIFIED, connection.getResponseCode());

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testSetModifiedForDifferentETag() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        executePutRequest(propertyKey, "TEST_VALUE");
        RequestResponse putResponse = executePutRequest(propertyKey, "TEST_VALUE2", Option.some(""));
        assertEquals(Response.SC_PRECONDITION_FAILED, putResponse.httpStatusCode);

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testMaximumPropertiesNotReached() throws Exception
    {
        final String propertyKeyPrefix = RandomStringUtils.randomAlphanumeric(15);
        for (int i = 0; i < 50; i++)
        {
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), "value").httpStatusCode;
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
        final String propertyKeyPrefix = RandomStringUtils.randomAlphanumeric(15);
        for (int i = 0; i < 50; i++)
        {
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), "value").httpStatusCode;
            assertEquals(Response.SC_CREATED, responseCode);
        }

        int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(51), "value").httpStatusCode;
        assertEquals(Response.SC_CONFLICT, responseCode);

        for (int i = 0; i < 50; i++)
        {
            assertDeleted(propertyKeyPrefix + String.valueOf(i));
        }
    }

    private String sendSuccessfulGetRequest(final String propertyKey)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, Option.option(runner.getSignedRequestHandler()));
        assertEquals(Response.SC_OK, connection.getResponseCode());
        return IOUtils.toString(connection.getInputStream());
    }

    private void assertNotAccessibleGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, signedRequestHandler);
        assertEquals(Response.SC_NOT_FOUND, connection.getResponseCode());
    }

    @Test
    public void testSuccessfulListRequest() throws IOException, URISyntaxException
    {
        // should clean all properties before running, else this test depends on previous test failures!
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, "TEST_VALUE", getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        URL url = new URL(restPath + "/properties");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        runner.getSignedRequestHandler().sign(url.toURI(), "GET", null, connection);

        assertEquals(Response.SC_OK, connection.getResponseCode());

        String response = IOUtils.toString(connection.getInputStream());
        RestAddOnPropertiesBean result = gson.fromJson(response, RestAddOnPropertiesBean.class);

        RestAddOnPropertiesBean expected = RestAddOnPropertiesBean.fromRestAddOnProperties(property);
        assertEquals(expected, result);
        assertDeleted(propertyKey);
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
                return input.replace("\"", "");
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
        private RequestResponse(final int httpStatusCode)
        {
            this(httpStatusCode, Option.<String>none());
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
