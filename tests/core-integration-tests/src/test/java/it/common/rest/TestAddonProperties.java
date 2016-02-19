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
import com.atlassian.plugin.connect.plugin.property.JsonCommon;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.servlet.SignedRequestHandler;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.product.TestedProductAccessor;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Functional test for add-on properties
 */
public class TestAddonProperties
{
    public static final int MAX_VALUE_SIZE = 1024 * 32;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final JsonNode JSON_ZERO = JsonCommon.parseStringToJson("0").get();
    private static final JsonNode JSON_ONE = JsonCommon.parseStringToJson("1").get();
    private static final JsonNode JSON_THREE = JsonCommon.parseStringToJson("3").get();

    private final String addonKey = "testAddonPropertyAddonKey";
    private final String baseUrl = TestedProductAccessor.get().getTestedProduct().getProductInstance().getBaseUrl();
    private final String restPath = baseUrl + "/rest/atlassian-connect/1/addons/" + addonKey;

    private ConnectRunner runner = null;
    private InstallHandlerServlet installHandlerServlet;

    @Before
    public void init() throws Exception
    {
        installHandlerServlet = new InstallHandlerServlet();
        runner = new ConnectRunner(baseUrl, addonKey)
                .addJWT(installHandlerServlet)
                .start();

        deleteAllAddonProperties();
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
        String jwt = AddonTestUtils.generateJwtSignature(HttpMethod.PUT, url.toURI(), addonKey, sharedSecret, baseUrl, null);

        URL longerUrl = new URL(restPath + "/properties/" + propertyKey + "?jwt=" + jwt);
        HttpURLConnection connection = (HttpURLConnection) longerUrl.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.getOutputStream().write(value.getBytes());

        int responseCode = connection.getResponseCode();
        assertEquals(Response.SC_CREATED, responseCode);

        deleteAndAssertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndGetProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddonProperty property = new RestAddonProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        final RestAddonProperty result = JSON.readValue(response, RestAddonProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

        deleteAndAssertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndGetNonAsciProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        final JsonNode jsonValue = JsonCommon.parseStringToJson("\"κόσμε\"").get();
        RestAddonProperty property = new RestAddonProperty(propertyKey, jsonValue, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, jsonValue).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddonProperty result = JSON.readValue(response, RestAddonProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

        deleteAndAssertDeleted(propertyKey);
    }

    @Test
    public void testCreateAndDeleteProperty() throws Exception
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddonProperty property = new RestAddonProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        deleteAndAssertDeleted(propertyKey);
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
        RestAddonProperty property = new RestAddonProperty(propertyKey, JSON_ZERO, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, JSON_ONE).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        int responseCode2 = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_OK, responseCode2);

        String response = sendSuccessfulGetRequestForPropertyKey(property.key);
        RestAddonProperty result = JSON.readValue(response, RestAddonProperty.class);
        assertThat(result, isEqualToIgnoringBaseUrl(property));

        int responseCode3 = executeDeleteRequest(property.key);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);
    }

    @Test
    public void testNoAccessFromSecondPlugin() throws Exception
    {
        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        ConnectRunner secondAddon = new ConnectRunner(baseUrl, "secondAddon")
                .addJWT(installHandlerServlet)
                .start();

        String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = executePutRequest(propertyKey, JSON_ONE).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        assertNotAccessibleGetRequest(propertyKey, Option.option(secondAddon.getSignedRequestHandler()));

        int responseCode3 = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode3);

        ConnectRunner.stopAndUninstallQuietly(secondAddon);
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
            deleteAndAssertDeleted(propertyKeyPrefix + String.valueOf(i));
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
            deleteAndAssertDeleted(propertyKeyPrefix + String.valueOf(i));
        }
    }

    @Test
    public void testSuccessfulListRequest() throws IOException, URISyntaxException
    {
        String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        RestAddonProperty property = new RestAddonProperty(propertyKey, JSON_ONE, getSelfForPropertyKey(propertyKey));

        int responseCode = executePutRequest(property.key, property.value).httpStatusCode;
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequestForPropertyList();
        RestAddonPropertiesBean result = JSON.readValue(response, RestAddonPropertiesBean.class);

        RestAddonPropertiesBean expected = RestAddonPropertiesBean.fromRestAddonProperties(property);
        assertThat(result, isEqualToIgnoringBaseUrl(expected));
        deleteAndAssertDeleted(propertyKey);
    }

    @Test
    public void testSuccessfulListRequestWithoutAuthentication() throws IOException, URISyntaxException
    {
        // Generate request to properties without authentication
        URL url = new URL(restPath + "/properties");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        assertEquals(Response.SC_UNAUTHORIZED, connection.getResponseCode());
    }

    private String getSelfForPropertyKey(final String propertyKey)
    {
        return restPath + "/properties/" + propertyKey;
    }

    private void deleteAndAssertDeleted(String propertyKey) throws IOException, URISyntaxException
    {
        int responseCode = executeDeleteRequest(propertyKey);
        assertEquals(Response.SC_NO_CONTENT, responseCode);
    }

    private void assertNotAccessibleGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = executeGetRequest(propertyKey, signedRequestHandler);
        assertEquals(Response.SC_UNAUTHORIZED, connection.getResponseCode());
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

    private void deleteAllAddonProperties() throws IOException, URISyntaxException
    {
        final String rawProperties = sendSuccessfulGetRequestForPropertyList();
        final RestAddonPropertiesBean restAddonProperties = JSON.readValue(rawProperties, RestAddonPropertiesBean.class);
        for (RestAddonPropertiesBean.RestAddonPropertyBean restAddonKey : restAddonProperties.keys)
        {
            deleteAndAssertDeleted(restAddonKey.key);
        }
    }

    private HttpURLConnection executeGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties/" + propertyKey + "?jsonValue=true");
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

    private Matcher<? super RestAddonProperty> isEqualToIgnoringBaseUrl(final RestAddonProperty expected)
    {
        return new TypeSafeMatcher<RestAddonProperty>()
        {
            @Override
            protected boolean matchesSafely(final RestAddonProperty property)
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

    private Matcher<? super RestAddonPropertiesBean> isEqualToIgnoringBaseUrl(final RestAddonPropertiesBean expected)
    {
        return new TypeSafeMatcher<RestAddonPropertiesBean>()
        {
            @Override
            protected boolean matchesSafely(final RestAddonPropertiesBean properties)
            {
                Iterable<RestAddonPropertiesBean.RestAddonPropertyBean> expectedBeans = ImmutableList.copyOf(expected.keys);
                Iterable<Matcher<? super RestAddonPropertiesBean.RestAddonPropertyBean>> transform = Iterables.transform(expectedBeans, new Function<RestAddonPropertiesBean.RestAddonPropertyBean, Matcher<? super RestAddonPropertiesBean.RestAddonPropertyBean>>()
                {
                    @Override
                    public Matcher<? super RestAddonPropertiesBean.RestAddonPropertyBean> apply(final RestAddonPropertiesBean.RestAddonPropertyBean input)
                    {
                        return isEqualToIgnoringBaseUrl(input);
                    }
                });

                Collection<Matcher<? super RestAddonPropertiesBean.RestAddonPropertyBean>> matchers = Lists.newArrayList(transform);
                return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers).matches(Arrays.asList(properties.keys));
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(expected);
            }
        };
    }

    private Matcher<? super RestAddonPropertiesBean.RestAddonPropertyBean> isEqualToIgnoringBaseUrl(final RestAddonPropertiesBean.RestAddonPropertyBean expected)
    {
        return new TypeSafeMatcher<RestAddonPropertiesBean.RestAddonPropertyBean>()
        {
            @Override
            protected boolean matchesSafely(final RestAddonPropertiesBean.RestAddonPropertyBean property)
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

        private RequestResponse(final int httpStatusCode, final Option<String> eTag)
        {
            this.httpStatusCode = httpStatusCode;
            this.eTag = eTag;
        }
    }

    private static class RestAddonProperty
    {
        @JsonProperty
        private final String key;
        @JsonProperty
        private final JsonNode value;
        @JsonProperty
        private final String self;

        public RestAddonProperty(@JsonProperty ("key") final String key, @JsonProperty ("value") final JsonNode value, @JsonProperty ("self") final String self)
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
            final RestAddonProperty other = (RestAddonProperty) obj;
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

    private static class RestAddonPropertiesBean
    {
        @JsonProperty
        private RestAddonPropertyBean[] keys;

        public RestAddonPropertiesBean() {}

        public RestAddonPropertiesBean(@JsonProperty RestAddonPropertyBean[] keys)
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
            final RestAddonPropertiesBean other = (RestAddonPropertiesBean) obj;
            return new EqualsBuilder().append(this.keys, other.keys).isEquals();
        }

        @Override
        public String toString()
        {
            return "RestAddonPropertiesBean{" +
                    "properties=" + keys +
                    '}';
        }

        static RestAddonPropertiesBean fromRestAddonProperties(RestAddonProperty... properties)
        {
            RestAddonPropertyBean[] propertyBeans = new RestAddonPropertyBean[properties.length];
            for (int i = 0; i < properties.length; i++)
            {
                propertyBeans[i] = new RestAddonPropertyBean(properties[i].self, properties[i].key);
            }
            return new RestAddonPropertiesBean(propertyBeans);
        }

        public static class RestAddonPropertyBean
        {
            @JsonProperty
            private String self;
            @JsonProperty
            private String key;

            public RestAddonPropertyBean() {}

            public RestAddonPropertyBean(@JsonProperty final String self, @JsonProperty final String key)
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
                final RestAddonPropertyBean other = (RestAddonPropertyBean) obj;
                return new EqualsBuilder().append(this.self, other.self).append(this.key, other.key).isEquals();
            }

            @Override
            public String toString()
            {
                return "RestAddonPropertyBean{" +
                        "self='" + self + '\'' +
                        ", key='" + key + '\'' +
                        '}';
            }
        }
    }
}
