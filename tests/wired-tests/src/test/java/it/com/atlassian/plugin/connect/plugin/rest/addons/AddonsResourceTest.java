package it.com.atlassian.plugin.connect.plugin.rest.addons;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.HttpHeaders;

import com.atlassian.httpclient.api.HttpStatus;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.license.LicenseHandler;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.util.request.RequestUtil;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonsResourceTest
{
    public static final int LICENSED_ADDON_KEY_COUNT = 100;
    public static final String TIMEBOMB_100_PLUGIN_LICENSE_PATH = "testfiles.licenses/timebomb-ac-test-json-0..99.license";

    private static String REST_BASE = "/atlassian-connect/1/addons";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final LicenseHandler licenseHandler;
    private final RequestUtil requestUtil;
    private final Random random;

    private String addonKey;
    private String addonSecret;

    public AddonsResourceTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                              ApplicationProperties applicationProperties, ConnectAddonRegistry connectAddonRegistry,
                              LicenseHandler licenseHandler)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.licenseHandler = licenseHandler;
        this.requestUtil = new RequestUtil(applicationProperties);
        this.random = new Random();
    }

    @Before
    public void setUp() throws IOException
    {
        InputStream licenseResource = getClass().getClassLoader().getResourceAsStream(TIMEBOMB_100_PLUGIN_LICENSE_PATH);
        String license = IOUtils.lineIterator(licenseResource, "UTF-8").nextLine();
        licenseHandler.setLicense(license);

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
        assertThat(response.getJsonBody().containsKey("message"), equalTo(true));
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
        AddonsInterpretation addonsRepresentation = response.getJsonBody(new TypeToken<AddonsInterpretation>()
        {
        }.getType());
        Iterable<String> addonKeys = Iterables.transform(addonsRepresentation.addons, new Function<AddonInterpretation, String>()
        {
            @Override
            public String apply(AddonInterpretation addonInterpretation)
            {
                return addonInterpretation.key;
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
        AddonInterpretation addonInterpretation = response.getJsonBody(AddonInterpretation.class);
        assertThat(addonInterpretation.key, equalTo(addonKey));
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByAddon() throws IOException
    {
        RequestUtil.Request request = getBuilderForGetAddon(addonKey)
                .setIncludeJwtAuthentication(addonKey, addonSecret).build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        AddonInterpretation addonInterpretation = response.getJsonBody(AddonInterpretation.class);
        assertThat(addonInterpretation.key, equalTo(addonKey));
        assertThat(addonInterpretation.state, equalTo("ENABLED"));
        assertThat(addonInterpretation.host, notNullValue());
        assertThat(addonInterpretation.host.contacts, notNullValue());
        assertThat(addonInterpretation.host.contacts, hasSize(1));
        assertThat(addonInterpretation.host.contacts.get(0).name, equalTo("Charlie Atlassian"));
        assertThat(addonInterpretation.host.contacts.get(0).email, equalTo("charlie@atlassian.com"));
        assertThat(addonInterpretation.license, notNullValue());
        assertThat(addonInterpretation.license.active, equalTo(true));
        assertThat(addonInterpretation.license.type, equalTo("TESTING"));
        assertThat(addonInterpretation.license.evaluation, equalTo(true));
        assertThat(addonInterpretation.license.supportEntitlementNumber, equalTo("SEN-500"));
    }

    @Test
    public void shouldReturnSingleAddonWhenRequestedByDisabledAddon() throws IOException
    {
        testPluginInstaller.disableAddon(addonKey);

        RequestUtil.Request request = getBuilderForGetAddon(addonKey)
                .setIncludeJwtAuthentication(addonKey, addonSecret).build();
        RequestUtil.Response response = requestUtil.makeRequest(request);

        assertResponseStatusCode(request, response, HttpStatus.OK);
        AddonInterpretation addonInterpretation = response.getJsonBody(AddonInterpretation.class);
        assertThat(addonInterpretation.key, equalTo(addonKey));
        assertThat(addonInterpretation.state, equalTo("DISABLED"));
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

        AddonsInterpretation addonsInterpretation = response.getJsonBody(AddonsInterpretation.class);
        assertEquals("No JSON add-ons should be returned", 0, addonsInterpretation.addons.size());
    }

    private String generateAddonKey()
    {
        // We license the specific add-on keys that this method may generate (ac-test-json-0 to ac-test-json-99)
        // using this license: /tests/wired-tests/src/test/resources/testfiles.licenses/timebomb-ac-test-json-0..99.license
        // This is necessary because changes for https://ecosystem.atlassian.net/browse/UPM-4851 mean
        // the wildcard license isn't returned if we ask for a specific add-on's license
        //
        // A range of 100 licenses, combined with our teardown method that uninstalls the add-on, makes conflicts very unlikely.
        return "ac-test-json-" + random.nextInt(LICENSED_ADDON_KEY_COUNT);
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

    /**
     * @see com.atlassian.plugin.connect.plugin.rest.data.RestAddons
     */
    private static class AddonsInterpretation
    {

        @JsonProperty
        public List<AddonInterpretation> addons;
    }

    /**
     * @see com.atlassian.plugin.connect.plugin.rest.data.RestLimitedAddon
     */
    private static class AddonInterpretation
    {

        @JsonProperty
        public String key;

        @JsonProperty
        public String version;

        @JsonProperty
        public String state;

        @JsonProperty
        public HostInterpretation host;

        @JsonProperty
        public AddonLicenseInterpretation license;
    }

    /**
     * @see com.atlassian.plugin.connect.plugin.rest.data.RestHost
     */
    private static class HostInterpretation
    {

        @JsonProperty
        public String product;

        @JsonProperty
        public List<ContactInterpretation> contacts;
    }

    /**
     * @see com.atlassian.plugin.connect.plugin.rest.data.RestAddonLicense
     */
    private static class AddonLicenseInterpretation
    {

        @JsonProperty
        public boolean active;

        @JsonProperty
        public String type;

        @JsonProperty
        public boolean evaluation;

        @JsonProperty
        public String supportEntitlementNumber;
    }

    /**
     * @see com.atlassian.plugin.connect.plugin.rest.data.RestContact
     */
    public class ContactInterpretation
    {

        @JsonProperty
        public String name;

        @JsonProperty
        public String email;

        public ContactInterpretation(String name, String email)
        {
            this.name = name;
            this.email = email;
        }
    }
}
