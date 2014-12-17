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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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
    private Plugin addon;

    public AddonsResourceTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                              ApplicationProperties applicationProperties, ConnectAddonRegistry connectAddonRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    @BeforeClass
    public void setUp() throws IOException
    {
        Iterable<String> installedAddonKeys = testPluginInstaller.getInstalledAddonKeys();
        if (installedAddonKeys.iterator().hasNext())
        {
            System.out.println("*** INSTALLED ADD-ONS: ");
            for (String key : installedAddonKeys)
            {
                System.out.println("*** " + key);
            }
        }

        this.testAuthenticator.authenticateUser("admin");
        this.addonKey = this.generateAddonKey();
        this.addon = this.installJsonAddon(this.addonKey);
        this.addonSecret = this.connectAddonRegistry.getSecret(this.addonKey);
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        if (null != addon)
        {
            try
            {
                testPluginInstaller.uninstallAddon(addon);
            }
            catch (IOException e)
            {
                LOG.error("Could not uninstall json addon", e);
            }
        }

        Iterable<String> installedAddonKeys = testPluginInstaller.getInstalledAddonKeys();
        if (installedAddonKeys.iterator().hasNext())
        {
            System.out.println("*** REMAINING ADD-ONS: ");
            for (String key : installedAddonKeys)
            {
                System.out.println("*** " + key);
            }
        }
    }

    @Test
    public void shouldReturnUnauthorizedWhenAnonymousMakesAnyRequest() throws IOException
    {
        for (RequestUtil.Request.Builder builder : this.getBuildersForAllRequests(this.addonKey))
        {
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = this.requestUtil.makeRequest(request);

            assertErrorResponseStatusCode(request, response, HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void shouldReturnUnauthorizedWhenAnonymousMakesRequestToInvalidAddon() throws IOException
    {
        for (RequestUtil.Request.Builder builder : this.getBuildersForAllDocumentRequests("invalid-key"))
        {
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = this.requestUtil.makeRequest(request);

            assertErrorResponseStatusCode(request, response, HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void shouldReturnForbiddenWhenUserMakesAnyRequest() throws IOException
    {
        for (RequestUtil.Request.Builder builder : this.getBuildersForAllRequests(this.addonKey))
        {
            builder = builder.setUsername("barney").setPassword("barney");
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = this.requestUtil.makeRequest(request);

            assertErrorResponseStatusCode(request, response, HttpStatus.FORBIDDEN);
        }
    }

    @Test
    public void shouldReturnForbiddenWhenAddonMakesForbiddenRequest() throws IOException
    {
        String otherAddonKey = this.generateAddonKey();
        Plugin otherAddon = this.installJsonAddon(otherAddonKey);

        List<RequestUtil.Request.Builder> builders = Lists.newArrayList(
                this.getBuilderForGetAddons(),
                this.getBuilderForGetAddon(otherAddonKey),
                this.getBuilderForUninstallAddon(this.addonKey),
                this.getBuilderForReinstallAddon(this.addonKey)
        );
        for (RequestUtil.Request.Builder builder : builders)
        {
            builder.setIncludeJwtAuthentication(this.addonKey, this.addonSecret);
            RequestUtil.Request request = builder.build();
            RequestUtil.Response response = this.requestUtil.makeRequest(request);

            assertResponseStatusCode(request, response, HttpStatus.FORBIDDEN);
        }

        this.testPluginInstaller.uninstallAddon(otherAddon);
    }

    @Test
    public void shouldReturnAddonsWhenRequestedByAdmin() throws IOException
    {
        String otherAddonKey = this.generateAddonKey();
        Plugin otherAddon = this.installJsonAddon(otherAddonKey);

        RequestUtil.Request request = this.getBuilderForGetAddons().setUsername("admin").setPassword("admin").build();
        RequestUtil.Response response = this.requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        Object[] expectedAddonKeys = new String[]{this.addonKey, otherAddonKey};
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

        this.testPluginInstaller.uninstallAddon(otherAddon);
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByAdmin() throws IOException
    {
        RequestUtil.Request request = this.getBuilderForGetAddon(this.addonKey).setUsername("admin").setPassword("admin").build();
        RequestUtil.Response response = this.requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        RestMinimalAddon addonRepresentation = response.getJsonBody(RestMinimalAddon.class);
        assertThat(addonRepresentation.getKey(), equalTo(this.addonKey));
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByAddon() throws IOException
    {
        RequestUtil.Request request = this.getBuilderForGetAddon(this.addonKey)
                .setIncludeJwtAuthentication(this.addonKey, this.addonSecret).build();
        RequestUtil.Response response = this.requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        RestMinimalAddon addonRepresentation = response.getJsonBody(RestMinimalAddon.class);
        assertThat(addonRepresentation.getKey(), equalTo(this.addonKey));
    }

    @Test
    public void shouldUninstallJsonAddon() throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.DELETE)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + this.addonKey))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertEquals("Addon should be deleted", 200, response.getStatusCode());
        assertEquals("Addon key is incorrect", this.addonKey, response.getJsonBody().get("key"));
        assertEquals("Addon version is incorrect", "1.0", response.getJsonBody().get("version"));
        assertEquals("Addon type is incorrect", "JSON", response.getJsonBody().get("type"));

        request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "?type=json"))
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
                .withBaseurl(this.testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withScopes(Sets.newHashSet(ScopeName.READ))
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return this.testPluginInstaller.installAddon(addonBean);
    }

    private List<RequestUtil.Request.Builder> getBuildersForAllRequests(String addonKey)
    {
        List<RequestUtil.Request.Builder> builders = this.getBuildersForAllDocumentRequests(addonKey);
        builders.add(0, this.getBuilderForGetAddons());
        return builders;
    }

    private List<RequestUtil.Request.Builder> getBuildersForAllDocumentRequests(String addonKey)
    {
        return Lists.newArrayList(
                this.getBuilderForGetAddon(addonKey),
                this.getBuilderForUninstallAddon(addonKey),
                this.getBuilderForReinstallAddon(addonKey)
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
        assertEquals(String.format("Expected status code %s not received for %s", status, requestString), status.code, response.getStatusCode());
    }

    private void assertErrorResponseStatusCode(RequestUtil.Request request, RequestUtil.Response response, HttpStatus status)
    {
        String requestString = String.format("%s %s", request.getMethod(), request.getUri());
        assertEquals(String.format("Expected status code %s not received for %s", status, requestString), status.code, response.getStatusCode());
        assertEquals(String.format("Status code not present in response body for %s", status, requestString), status.code, getStatusCode(response));
    }
}
