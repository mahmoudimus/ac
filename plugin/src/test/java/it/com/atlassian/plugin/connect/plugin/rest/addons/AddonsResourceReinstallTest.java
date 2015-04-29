package it.com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.google.gson.JsonParser;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.request.RequestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.atlassian.plugin.connect.util.AddonUtil.randomWebItemBean;
import static it.com.atlassian.plugin.connect.plugin.installer.AbstractAddonLifecycleTest.DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith (AtlassianPluginsTestRunner.class)
public class AddonsResourceReinstallTest
{
    private static final Logger LOG = LoggerFactory.getLogger(AddonsResourceReinstallTest.class);

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final AddonTestFilterResults testFilterResults;
    private final RequestUtil requestUtil;
    private final DarkFeatureManager darkFeatureManager;

    private static final String REST_BASE = "/atlassian-connect/1/addons";
    public static final String SHARED_SECRET_FIELD_NAME = "sharedSecret";
    public static final String INSTALLED = "/installed";

    public AddonsResourceReinstallTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,
                                       AddonTestFilterResults testFilterResults, ApplicationProperties applicationProperties,
                                       DarkFeatureManager darkFeatureManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.requestUtil = new RequestUtil(applicationProperties);
        this.darkFeatureManager = darkFeatureManager;
    }

    @BeforeClass
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void reinstallJsonAddon() throws IOException
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        Plugin plugin = testPluginInstaller.installAddon(createAddonBean());
        String addonKey = plugin.getKey();

        try
        {
            ServletRequestSnapshot installRequest = testFilterResults.getRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, INSTALLED);
            String installPayload = installRequest.getEntity();

            String originalSharedSecret = getSharedSecret(installPayload);

            RequestUtil.Response getAddonResponse = getAddonByKey(addonKey);

            assertEquals("Addon should be found", 200, getAddonResponse.getStatusCode());
            assertEquals("Addon key is incorrect", addonKey, getAddonResponse.getJsonBody().get("key"));

            // REINSTALL ADD-ON
            RequestUtil.Request request = requestUtil.requestBuilder()
                    .setMethod(HttpMethod.PUT)
                    .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey + "/reinstall"))
                    .setUsername("admin")
                    .setPassword("admin")
                    .build();

            RequestUtil.Response reinstallResponse = requestUtil.makeRequest(request);
            assertEquals("Addon should be found", 200, reinstallResponse.getStatusCode());
            assertEquals("Addon key is incorrect", addonKey, reinstallResponse.getJsonBody().get("key"));

            ServletRequestSnapshot reinstallRequest = testFilterResults.getRequest(addonKey, INSTALLED);
            String reinstallPayload = reinstallRequest.getEntity();
            String reinstallSharedSecret = getSharedSecret(reinstallPayload);
            assertFalse("Shared secret of reinstalled request should not be the same as original shared secret", originalSharedSecret.equals(reinstallSharedSecret));
        }
        finally
        {
            try
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
            catch (IOException e)
            {
                LOG.error("Could not uninstall json addon", e);
            }
        }
    }

    private String getSharedSecret(final String installPayload) {return new JsonParser().parse(installPayload).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString();}

    private RequestUtil.Response getAddonByKey(String addonKey) throws IOException
    {
        RequestUtil.Request request = requestUtil.requestBuilder()
                .setMethod(HttpMethod.GET)
                .setUri(requestUtil.getApplicationRestUrl(REST_BASE + "/" + addonKey))
                .setUsername("admin")
                .setPassword("admin")
                .build();

        return requestUtil.makeRequest(request);
    }

    private ConnectAddonBean createAddonBean() throws IOException
    {
        String key = "ac-test-json-" + System.currentTimeMillis();
        return ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withDescription(getClass().getCanonicalName())
                .withAuthentication(
                        AuthenticationBean.newAuthenticationBean()
                                .withType(AuthenticationType.JWT)
                                .build()
                )
                .withLifecycle(
                        LifecycleBean.newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withModule("webItems", randomWebItemBean())
                .build();
    }
}

