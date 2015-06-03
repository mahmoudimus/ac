package it.com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonPrecannedResponseHelper;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.JwtTestVerifier;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.gson.JsonParser;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonLifecycleJwtTest extends AbstractAddonLifecycleTest
{
    private final DarkFeatureManager darkFeatureManager;
    private final AddonPrecannedResponseHelper addonPrecannedResponseHelper;

    public AddonLifecycleJwtTest(TestPluginInstaller testPluginInstaller,
                                 TestAuthenticator testAuthenticator,
                                 AddonTestFilterResults testFilterResults,
                                 ConnectApplinkManager connectApplinkManager,
                                 ConnectUserService connectUserService,
                                 UserManager userManager,
                                 ApplicationService applicationService,
                                 ApplicationManager applicationManager,
                                 DarkFeatureManager darkFeatureManager,
                                 AddonPrecannedResponseHelper addonPrecannedResponseHelper,
                                 ConnectAddonRegistry connectAddonRegistry)
    {
        super(testPluginInstaller, testAuthenticator, testFilterResults, connectApplinkManager, connectUserService, userManager, applicationService, applicationManager, darkFeatureManager, connectAddonRegistry);
        this.darkFeatureManager = darkFeatureManager;
        this.addonPrecannedResponseHelper = addonPrecannedResponseHelper;
    }

    @Override
    protected boolean signCallbacksWithJwt()
    {
        return true;
    }

    @Before
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
                testPluginInstaller.uninstallAddon(plugin);
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
                testPluginInstaller.uninstallAddon(plugin);
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
                testPluginInstaller.uninstallAddon(plugin);
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

            testPluginInstaller.uninstallAddon(plugin);
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
                testPluginInstaller.uninstallAddon(plugin);
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

            testPluginInstaller.uninstallAddon(plugin);
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
                testPluginInstaller.uninstallAddon(plugin);
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
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void aFailedReinstallationPreservesPreviousUninstalledState() throws Exception
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        testFailedReinstallation(true);
    }

    @Test
    public void aFailedReinstallationPreservesPreviousUninstalledStateWhenTheDarkFeatureIsEnabled() throws Exception
    {
        darkFeatureManager.enableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);

        try
        {
            testFailedReinstallation(false);
        }
        finally
        {
            darkFeatureManager.disableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);
        }
    }

    private void testFailedReinstallation(final boolean signsWithPreviousSharedSecret) throws IOException, JwtParseException, JwtUnknownIssuerException, JwtVerificationException, JwtIssuerLacksSharedSecretException
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);
            addonKey = plugin.getKey();
            final ServletRequestSnapshot firstInstallRequest = testFilterResults.getRequest(addonKey, INSTALLED);
            final String firstSharedSecret = parseSharedSecret(firstInstallRequest);
            final String clientKey = parseClientKey(firstInstallRequest);

            testPluginInstaller.uninstallAddon(plugin);
            addonPrecannedResponseHelper.queuePrecannedResponse(testPluginInstaller.getInternalAddonBaseUrlSuffix(addonKey, INSTALLED), 404);

            try
            {
                plugin = testPluginInstaller.installAddon(addon); // fail
                fail("this installation attempt should have failed");
            }
            catch (PluginInstallException e)
            {
                plugin = null; // this is supposed to happen; see the pre-canned response above
            }

            plugin = testPluginInstaller.installAddon(addon); // successful re-installation following a failed re-installation
            ServletRequestSnapshot secondInstallRequest = testFilterResults.getRequest(addonKey, INSTALLED);

            assertEquals(true, secondInstallRequest.hasJwt());
            final String keyForSigningReinstallRequest = signsWithPreviousSharedSecret ? firstSharedSecret : parseSharedSecret(secondInstallRequest);
            JwtTestVerifier verifier = new JwtTestVerifier(keyForSigningReinstallRequest, clientKey);
            assertTrue("JWT token should be signed with the shared secret '" + keyForSigningReinstallRequest + "'", verifier.jwtAndClientAreValid(JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX + secondInstallRequest.getJwtToken()));
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);

            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }
}
