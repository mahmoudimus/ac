package it.com.atlassian.plugin.connect.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.JwtTestVerifier;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.user.UserManager;
import com.google.gson.JsonParser;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleJwtTest extends AbstractAddonLifecycleTest
{
    public AddonLifecycleJwtTest(TestPluginInstaller testPluginInstaller,
                                 TestAuthenticator testAuthenticator,
                                 AddonTestFilterResults testFilterResults,
                                 ConnectApplinkManager connectApplinkManager,
                                 ConnectAddOnUserService connectAddOnUserService,
                                 UserManager userManager,
                                 ApplicationService applicationService,
                                 ApplicationManager applicationManager,
                                 DarkFeatureManager darkFeatureManager)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, connectApplinkManager, connectAddOnUserService, userManager, applicationService, applicationManager, darkFeatureManager);
    }

    @Override
    protected boolean signCallbacksWithJwt()
    {
        return true;
    }

    @BeforeClass
    public void setup() throws Exception
    {
        testAuthenticator.authenticateUser("admin");
        
        initBeans(newAuthenticationBean().withType(AuthenticationType.JWT).build());
    }

    @Test
    public void installPostContainsSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            
            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String payload = request.getEntity();

            boolean hasSharedSecret = new JsonParser().parse(payload).getAsJsonObject().has(SHARED_SECRET_FIELD_NAME);
            assertTrue("field " + SHARED_SECRET_FIELD_NAME + " not found in request payload: " + payload, hasSharedSecret);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void enabledPostContainsValidSharedSecret() throws Exception
    {
        ConnectAddonBean addon = installAndEnabledBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;
            
            ServletRequestSnapshot installRequest = testFilterResults.getRequest(addonKey, INSTALLED);
            String installPayload = installRequest.getEntity();

            String sharedSecret = new JsonParser().parse(installPayload).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString();
            String clientKey = new JsonParser().parse(installPayload).getAsJsonObject().get(CLIENT_KEY_FIELD_NAME).getAsString();

            WaitUntil.invoke(new WaitUntil.WaitCondition()
            {
                @Override
                public boolean isFinished()
                {
                    return null != testFilterResults.getRequest(finalKey, ENABLED);
                }

                @Override
                public String getWaitMessage()
                {
                    return "waiting for enable webhook post...";
                }
            },5);

            ServletRequestSnapshot enableRequest = testFilterResults.getRequest(addonKey, ENABLED);

            String jwtToken = enableRequest.getHeaders().get(JwtConstants.HttpRequests.AUTHORIZATION_HEADER.toLowerCase());

            JwtTestVerifier verifier = new JwtTestVerifier(sharedSecret, clientKey);

            assertTrue("unverified jwt token", verifier.jwtAndClientAreValid(jwtToken));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, ENABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void installPostContainsNoUserKey() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String payload = request.getEntity();
            boolean hasUserKey = new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME);

            assertTrue("field " + USER_KEY_FIELD_NAME + " found in request payload: " + payload, !hasUserKey);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsValidJwt() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnapshot installRequest = testFilterResults.getRequest(addonKey, INSTALLED);
            String installPayload = installRequest.getEntity();

            String sharedSecret = new JsonParser().parse(installPayload).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString();
            String clientKey = new JsonParser().parse(installPayload).getAsJsonObject().get(CLIENT_KEY_FIELD_NAME).getAsString();

            testPluginInstaller.uninstallJsonAddon(plugin);
            plugin = null;

            ServletRequestSnapshot uninstallRequest = testFilterResults.getRequest(addonKey, UNINSTALLED);

            String jwtToken = uninstallRequest.getHeaders().get(JwtConstants.HttpRequests.AUTHORIZATION_HEADER.toLowerCase());

            JwtTestVerifier verifier = new JwtTestVerifier(sharedSecret, clientKey);

            assertTrue("unverified jwt token", verifier.jwtAndClientAreValid(jwtToken));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void uninstallPostContainsNoUserKey() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallJsonAddon(plugin);
            plugin = null;

            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            String payload = request.getEntity();
            boolean hasUserKey = new JsonParser().parse(payload).getAsJsonObject().has(USER_KEY_FIELD_NAME);

            assertTrue("field " + USER_KEY_FIELD_NAME + " found in request payload: " + payload, !hasUserKey);

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void appLinkIsCreatedWithCorrectParameters() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ApplicationLink appLink = connectApplinkManager.getAppLink(addon.getKey());

            assertNotNull((appLink));
            assertEquals(addon.getBaseUrl(), appLink.getDisplayUrl().toString());
            assertEquals("addon_" + addon.getKey(), appLink.getProperty("user.key"));
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

}
