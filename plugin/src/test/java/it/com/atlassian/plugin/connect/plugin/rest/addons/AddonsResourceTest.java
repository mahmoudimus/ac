package it.com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonsResourceTest
{
    private static final Logger LOG = LoggerFactory.getLogger(AddonsResourceTest.class);
    private static String REST_BASE = "/atlassian-connect/1/addons";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final RequestUtil requestUtil;

    private String addonKey;
    private String addonSecret;

    public AddonsResourceTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                              ApplicationProperties applicationProperties, ConnectAddonRegistry connectAddonRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    @BeforeClass
    public void setUpClass()
    {
        Iterable<String> installedAddonKeys = testPluginInstaller.getInstalledAddonKeys();
        if (installedAddonKeys.iterator().hasNext())
        {
            System.out.println("*** INSTALLED ADD-ONS: ");
            for (String key : installedAddonKeys)
            {
                LOG.debug("*** " + key);
            }
        }
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        addonKey = generateAddonKey();
        installJsonAddon(addonKey);
        addonSecret = connectAddonRegistry.getSecret(addonKey);
    }

    @After
    public void tearDown() throws IOException
    {
        for (String key : testPluginInstaller.getInstalledAddonKeys())
        {
            testPluginInstaller.uninstallAddon(key);
        }
    }

    @Test
    public void shouldReturnUnauthorizedWhenAnonymousMakesAnySysAdminRestrictedRequest() throws IOException
    {
        for (RequestUtil.Request.Builder builder : getBuildersForAllSysAdminRequests(addonKey))
        {
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = requestUtil.makeRequest(request);

            assertErrorResponseStatusCode(request, response, HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void shouldReturnUnauthorizedWithAddonChallengeWhenAnonymousMakesAddonRestrictedRequest() throws IOException
    {
        RequestUtil.Request request = getBuilderForGetAddon(addonKey).build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.UNAUTHORIZED);
        assertResponseHeaderValue(response, HttpHeaders.WWW_AUTHENTICATE, "JWT realm=\"Atlassian Connect\"");
    }

    @Test
    public void shouldReturnForbiddenWhenUserMakesAnyRequest() throws IOException
    {
        for (RequestUtil.Request.Builder builder : getBuildersForAllRequests(addonKey))
        {
            builder = builder.setUsername("barney").setPassword("barney");
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = requestUtil.makeRequest(request);

            assertResponseStatusCode(request, response, HttpStatus.FORBIDDEN);
        }
    }

    @Test
    public void shouldReturnForbiddenWhenAddonMakesForbiddenRequest() throws IOException
    {
        String otherAddonKey = generateAddonKey();
        installJsonAddon(otherAddonKey);

        List<RequestUtil.Request.Builder> builders = Lists.newArrayList(
                getBuilderForGetAddons(),
                getBuilderForGetAddon(otherAddonKey),
                getBuilderForUninstallAddon(addonKey),
                getBuilderForReinstallAddon(addonKey)
        );
        for (RequestUtil.Request.Builder builder : builders)
        {
            builder.setIncludeJwtAuthentication(addonKey, addonSecret);
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = requestUtil.makeRequest(request);

            assertResponseStatusCode(request, response, HttpStatus.FORBIDDEN);
        }
    }

    @Test
    public void shouldReturnAddonsWhenRequestedByAdmin() throws IOException
    {
        String otherAddonKey = generateAddonKey();
        Plugin otherAddon = installJsonAddon(otherAddonKey);

        RequestUtil.Request request = getBuilderForGetAddons().setUsername("admin").setPassword("admin").build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        Object[] expectedAddonKeys = new String[]{addonKey, otherAddonKey};
        RestAddons<RestMinimalAddon> addonRepresentations = response.getJsonBody(new TypeToken<RestAddons<RestMinimalAddon>>()
        {
        }.getType());
        Iterable<String> addonKeys = Iterables.transform(addonRepresentations.getAddons(), new Function<RestMinimalAddon, String>()
        {
            @Override
            public String apply(RestMinimalAddon addonRepresentation)
            {
                return addonRepresentation.getKey();
            }
        });
        assertThat("Wrong addons in response", addonKeys, containsInAnyOrder(expectedAddonKeys));

        testPluginInstaller.uninstallAddon(otherAddon);
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByAdmin() throws IOException
    {
        RequestUtil.Request request = getBuilderForGetAddon(addonKey).setUsername("admin").setPassword("admin").build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        RestMinimalAddon addonRepresentation = response.getJsonBody(RestMinimalAddon.class);
        assertThat(addonRepresentation.getKey(), equalTo(addonKey));
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByAddon() throws IOException
    {
        RequestUtil.Request request = getBuilderForGetAddon(addonKey)
                .setIncludeJwtAuthentication(addonKey, addonSecret).build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        RestMinimalAddon addonRepresentation = response.getJsonBody(RestMinimalAddon.class);
        assertThat(addonRepresentation.getKey(), equalTo(addonKey));
        assertThat((String) response.getJsonBody().get("state"), equalTo("ENABLED"));
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByDisabledAddon() throws IOException
    {
        testPluginInstaller.disableAddon(addonKey);

        RequestUtil.Request request = getBuilderForGetAddon(addonKey)
                .setIncludeJwtAuthentication(addonKey, addonSecret).build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        RestMinimalAddon addonRepresentation = response.getJsonBody(RestMinimalAddon.class);
        assertThat(addonRepresentation.getKey(), equalTo(addonKey));
        assertThat((String) response.getJsonBody().get("state"), equalTo("DISABLED"));
    }

    @Test
    public void shouldUninstallJsonAddon() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be deleted", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", addonKey, response.getJsonBody().get("key"));
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));

        request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        response = requestUtil.makeRequest(request);
        assertEquals("Addons resource should return 200", 200, response.getStatusCode());

        RestAddons<RestAddon> addonRepresentations = response.getJsonBody(RestAddons.class);
        assertEquals("No JSON add-ons should be returned", 0, addonRepresentations.getAddons().size());
    }

    private String generateAddonKey()
    {
        return "ac-test-json-" + UUID.randomUUID();
    }

    private Plugin installJsonAddon(String addonKey) throws IOException
    {
        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(addonKey)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .withLicensing(true)
                .build();

        return testPluginInstaller.installAddon(addonBean);
    }

    private List<RequestUtil.Request.Builder> getBuildersForAllRequests(String addonKey)
    {
        List<RequestUtil.Request.Builder> builders = getBuildersForAllSysAdminRequests(addonKey);
        builders.add(0, getBuilderForGetAddon(addonKey));
        return builders;
    }

    private List<RequestUtil.Request.Builder> getBuildersForAllSysAdminRequests(String addonKey)
    {
        return Lists.newArrayList(
                getBuilderForGetAddons(),
                getBuilderForUninstallAddon(addonKey),
                getBuilderForReinstallAddon(addonKey)
        );
    }

    private RequestUtil.Request.Builder getBuilderForGetAddons()
    {
        return requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE));
    }

    private RequestUtil.Request.Builder getBuilderForGetAddon(String addonKey)
    {
        return requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey));
    }

    private RequestUtil.Request.Builder getBuilderForUninstallAddon(String addonKey)
    {
        return requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey));
    }

    private RequestUtil.Request.Builder getBuilderForReinstallAddon(String addonKey)
    {
        return requestUtil.requestBuilder()
                .setMethod(HttpMethod.PUT)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey + "/reinstall"));
    }

    private int getStatusCode(RequestUtil.Response response)
    {
        return ((Double) response.getJsonBody().get("status-code")).intValue();
    }

    private void assertResponseStatusCode(RequestUtil.Request request, RequestUtil.Response response, HttpStatus status)
    {
        String requestString = String.format("%s %s", request.getMethod(), request.getUri());
        try
        {
            assertEquals(String.format("Expected status code %s not received for %s", status, requestString), status.code, response.getStatusCode());
        }
        catch (AssertionError e)
        {
            // TODO Remove when Confluence has been upgraded to atlassian-rest-common 2.9.12, including the fix for REST-286
            if (status.equals(HttpStatus.FORBIDDEN))
            {
                assertResponseStatusCode(request, response, HttpStatus.UNAUTHORIZED);
            }
            else
            {
                throw e;
            }
        }
    }

    private void assertResponseHeaderValue(RequestUtil.Response response, String headerName, String value)
    {
        Map<String, List<String>> headerFields = response.getHeaderFields();
        assertThat("Expected response header not set", headerFields.keySet(), hasItem(headerName));
        assertThat(String.format("Unexpected response value for header %s", headerName), headerFields.get(headerName), hasItem(value));
    }

    private void assertErrorResponseStatusCode(RequestUtil.Request request, RequestUtil.Response response, HttpStatus status)
    {
        String requestString = String.format("%s %s", request.getMethod(), request.getUri());
        assertThat(String.format("Expected status code %s not received for %s", status, requestString), status.code, equalTo(response.getStatusCode()));
        assertThat(String.format("Status code not present in response body for %s", status, requestString), status.code, equalTo(getStatusCode(response)));
    }
}
