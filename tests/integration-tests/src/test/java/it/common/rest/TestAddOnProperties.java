package it.common.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.plugin.util.JsonCommon;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.eclipse.jetty.server.Response;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.servlet.InstallHandlerServlet;

import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getTestedProduct;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Functional test for add-on properties
 */
public class TestAddOnProperties
{
    public static final int MAX_VALUE_SIZE = 1024 * 32;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final JsonNode JSON_ZERO = JsonCommon.parseStringToJson("0").get();
    private static final JsonNode JSON_ONE = JsonCommon.parseStringToJson("1").get();
    private static final JsonNode JSON_THREE = JsonCommon.parseStringToJson("3").get();

    private final String addOnKey = "testAddOnPropertyAddOnKey";
    private final String baseUrl = getTestedProduct().getProductInstance().getBaseUrl();
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
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        String value = "0";

        URL url = new URL(restPath + "/properties/" + propertyKey);

        String sharedSecret = checkNotNull(installHandlerServlet.getInstallPayload().getSharedSecret());
        String jwt = AddonTestUtils.generateJwtSignature(HttpMethod.PUT, url.toURI(), addOnKey, sharedSecret, baseUrl, null);

        URL longerUrl = new URL(restPath + "/properties/" + propertyKey + "?jwt=" + jwt);
        HttpURLConnection connection = (HttpURLConnection) longerUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.getOutputStream().write(value.getBytes());

        int responseCode = connection.getResponseCode();
        assertEquals(Response.SC_CREATED, responseCode);

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndGetProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        final RestAddOnProperty result = JSON.readValue(response, RestAddOnProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndGetNonAsciProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        final JsonNode jsonValue = JsonCommon.parseStringToJson("\"κόσμε\"").get();
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, jsonValue, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, jsonValue).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddOnProperty result = JSON.readValue(response, RestAddOnProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

        assertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndDeleteProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

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
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, JSON_ONE).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        int responseCode2 = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_OK, responseCode2);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddOnProperty result = JSON.readValue(response, RestAddOnProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

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

        int responseCode = executePutRequest(propertyKey, JSON_ONE).httpStatusCode;
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
        JsonNode tooBigValue = JsonNodeFactory.instance.textNode(StringUtils.repeat(" ", MAX_VALUE_SIZE + 1));

        int responseCode = executePutRequest(propertyKey, tooBigValue).httpStatusCode;
        assertEquals(Response.SC_FORBIDDEN, responseCode);
    }

    @Test
    public void testMaximumPropertiesNotReached() throws Exception
    {
        String propertyKeyPrefix = RandomStringUtils.randomAlphanumeric(15);
        for (int i = 0; i < 50; i++)
        {
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), JSON_THREE).httpStatusCode;
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
            int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(i), JSON_THREE).httpStatusCode;
            assertEquals(Response.SC_CREATED, responseCode);
        }

        int responseCode = executePutRequest(propertyKeyPrefix + String.valueOf(51), JSON_THREE).httpStatusCode;
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
        RestAddOnProperty property = new RestAddOnProperty(propertyKey, JSON_ONE, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyList();
        RestAddOnPropertiesBean result = JSON.readValue(response, RestAddOnPropertiesBean.class);

        RestAddOnPropertiesBean expected = RestAddOnPropertiesBean.fromRestAddOnProperties(property);
        assertThat(result, isEqualToIgnoringBaseUrl(expected));
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

    private RequestResponse executePutRequest(final String propertyKey, JsonNode value) throws IOException, URISyntaxException
    {
        return executePutRequest(propertyKey, value, Option.<String>none());
    }

    private RequestResponse executePutRequest(final String propertyKey, JsonNode value, Option<String> eTag) throws IOException, URISyntaxException
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
        connection.getOutputStream().write(value.toString().getBytes());

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

    private Matcher<? super RestAddOnProperty> isEqualToIgnoringBaseUrl(final RestAddOnProperty expected)
    {
        return new TypeSafeMatcher<RestAddOnProperty>()
        {
            @Override
            protected boolean matchesSafely(final RestAddOnProperty property)
            {
                String urlWithoutBaseUrl = expected.self.substring(baseUrl.length());
                return new EqualsBuilder()
                        .append(property.key, expected.key)
                        .append(property.value, expected.value)
                        .isEquals()
                        && property.self.endsWith(urlWithoutBaseUrl);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(expected);
            }
        };
    }

    private Matcher<? super RestAddOnPropertiesBean> isEqualToIgnoringBaseUrl(final RestAddOnPropertiesBean expected)
    {
        return new TypeSafeMatcher<RestAddOnPropertiesBean>()
        {
            @Override
            protected boolean matchesSafely(final RestAddOnPropertiesBean properties)
            {
                Iterable<RestAddOnPropertiesBean.RestAddOnPropertyBean> expectedBeans = Arrays.asList(expected.keys);
                Iterable<Matcher<? super RestAddOnPropertiesBean.RestAddOnPropertyBean>> transform = Iterables.transform(expectedBeans, new Function<RestAddOnPropertiesBean.RestAddOnPropertyBean, Matcher<? super RestAddOnPropertiesBean.RestAddOnPropertyBean>>()
                {
                    @Override
                    public Matcher<? super RestAddOnPropertiesBean.RestAddOnPropertyBean> apply(final RestAddOnPropertiesBean.RestAddOnPropertyBean input)
                    {
                        return isEqualToIgnoringBaseUrl(input);
                    }
                });

                Collection<Matcher<? super RestAddOnPropertiesBean.RestAddOnPropertyBean>> matchers = Lists.newArrayList(transform);
                return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers).matches(Arrays.asList(properties.keys));
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(expected);
            }
        };
    }

    private Matcher<? super RestAddOnPropertiesBean.RestAddOnPropertyBean > isEqualToIgnoringBaseUrl(final RestAddOnPropertiesBean.RestAddOnPropertyBean expected)
    {
        return new TypeSafeMatcher<RestAddOnPropertiesBean.RestAddOnPropertyBean >()
        {
            @Override
            protected boolean matchesSafely(final RestAddOnPropertiesBean.RestAddOnPropertyBean  property)
            {
                String urlWithoutBaseUrl = expected.self.substring(baseUrl.length());
                return new EqualsBuilder()
                        .append(property.key, expected.key)
                        .isEquals()
                        && property.self.endsWith(urlWithoutBaseUrl);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(expected);
            }
        };
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
        private final JsonNode value;
        @JsonProperty
        private final String self;

        public RestAddOnProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final JsonNode value, @JsonProperty ("self") final String self)
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
                    "properties=" + Arrays.toString(keys) +
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
