package it;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.SignedRequestHandler;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import it.servlet.InstallHandlerServlet;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.jetty.server.Response;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        int responseCode = craftPutRequest(property.key, property.value);
        assertEquals(Response.SC_CREATED, responseCode);

        String response = sendSuccessfulGetRequest(property.key);
        RestAddOnProperty result = gson.fromJson(response, RestAddOnProperty.class);
        assertEquals(property, result);
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

        int responseCode = craftPutRequest(property.key, "TEST_VALUE_2");
        assertEquals(Response.SC_CREATED, responseCode);

        int responseCode2 = craftPutRequest(property.key, property.value);
        assertEquals(Response.SC_OK, responseCode2);

        String response = sendSuccessfulGetRequest(property.key);
        RestAddOnProperty result = gson.fromJson(response, RestAddOnProperty.class);
        assertEquals(property, result);
    }

    @Test
    public void testNoAccessFromSecondPlugin() throws Exception
    {
        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        ConnectRunner secondAddOn = new ConnectRunner(baseUrl, "secondAddOn")
                .addJWT(installHandlerServlet)
                .start();

        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        int responseCode = craftPutRequest(propertyKey, "TEST_VALUE_2");
        assertEquals(Response.SC_CREATED, responseCode);

        assertNotAccessibleGetRequest(propertyKey, Option.option(secondAddOn.getSignedRequestHandler()));

        ConnectRunner.stopAndUninstallQuietly(secondAddOn);
    }

    @Test
    public void testNoAccessWithoutPluginKeyInHeader() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);

        HttpURLConnection connection = craftGetRequest(propertyKey, Option.<SignedRequestHandler>none());
        assertEquals(Response.SC_UNAUTHORIZED, connection.getResponseCode());
    }

    @Test
    public void testPutValueTooBigReturnsForbidden() throws Exception
    {
        final String propertyKey = RandomStringUtils.randomAlphanumeric(15);
        final String tooBigValue = StringUtils.repeat(" ", MAX_VALUE_SIZE + 1);

        int responseCode = craftPutRequest(propertyKey, tooBigValue);
        assertEquals(Response.SC_FORBIDDEN, responseCode);
    }

    private String sendSuccessfulGetRequest(final String propertyKey)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = craftGetRequest(propertyKey, Option.option(runner.getSignedRequestHandler()));
        assertEquals(Response.SC_OK, connection.getResponseCode());
        Optional<String> json = getJSON(connection);
        assertTrue(json.isPresent());
        return json.get();
    }

    private void assertNotAccessibleGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler)
            throws IOException, URISyntaxException
    {
        HttpURLConnection connection = craftGetRequest(propertyKey, signedRequestHandler);
        assertEquals(Response.SC_FORBIDDEN, connection.getResponseCode());
    }

    private HttpURLConnection craftGetRequest(final String propertyKey, final Option<SignedRequestHandler> signedRequestHandler) throws IOException, URISyntaxException
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

    private int craftPutRequest(final String propertyKey, String value) throws IOException, URISyntaxException
    {
        URL url = new URL(restPath + "/properties/" + propertyKey);
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.setRequestMethod("PUT");
        runner.getSignedRequestHandler().sign(url.toURI(), "PUT", null, yc);
        yc.setDoOutput(true);
        yc.getOutputStream().write(value.getBytes());
        return yc.getResponseCode();
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
    public Optional<String> getJSON(HttpURLConnection c) {
        try
        {
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    br.close();
                    return Optional.of(sb.toString());
            }
        }
        catch (IOException e) {}
        return Optional.absent();
    }
}
