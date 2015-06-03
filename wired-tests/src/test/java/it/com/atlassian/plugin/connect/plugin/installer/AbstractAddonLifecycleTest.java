package it.com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.user.ConnectUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.JwtTestVerifier;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.gson.JsonParser;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static com.google.common.collect.Sets.newHashSet;
import static it.com.atlassian.plugin.connect.util.matcher.ParamMatchers.isVersionNumber;
import static it.com.atlassian.plugin.connect.util.request.HeaderUtil.getVersionHeader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAddonLifecycleTest
{
    public static final String PLUGIN_KEY = "my-lifecycle-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String INSTALLED = "/installed";
    public static final String ENABLED = "/enabled";
    public static final String DISABLED = "/disabled";
    public static final String UNINSTALLED = "/uninstalled";
    public static final String SHARED_SECRET_FIELD_NAME = "sharedSecret";
    public static final String CLIENT_KEY_FIELD_NAME = "clientKey";
    public static final String USER_KEY_FIELD_NAME = "userKey";
    public static final String POST = "POST";
    public static final String CONNECT_ADDON_USER_GROUP = "atlassian-addons";
    public static final String ADD_ON_USER_KEY_PREFIX = "addon_";
    public static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge

    public static final String DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY = "connect.lifecycle.install.sign_with_prev_key.disable";

    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    protected final AddonTestFilterResults testFilterResults;
    protected final ConnectApplinkManager connectApplinkManager;
    protected final ConnectUserService connectUserService;
    private final UserManager userManager;
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final DarkFeatureManager darkFeatureManager;
    private final ConnectAddonRegistry connectAddonRegistry;

    protected ConnectAddonBean baseBean;
    protected ConnectAddonBean installOnlyBean;
    protected ConnectAddonBean uninstallOnlyBean; // not valid for JWT tests - JWT requires an installed callback
    protected ConnectAddonBean installAndEnabledBean;
    protected ConnectAddonBean installAndDisabledBean;
    protected ConnectAddonBean installAndUninstallBean;
    protected ConnectAddonBean fullLifecycleBean;

    protected AbstractAddonLifecycleTest(TestPluginInstaller testPluginInstaller,
                                         TestAuthenticator testAuthenticator,
                                         AddonTestFilterResults testFilterResults,
                                         ConnectApplinkManager connectApplinkManager,
                                         ConnectUserService connectUserService,
                                         UserManager userManager,
                                         ApplicationService applicationService,
                                         ApplicationManager applicationManager,
                                         DarkFeatureManager darkFeatureManager,
                                         ConnectAddonRegistry connectAddonRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
        this.connectUserService = connectUserService;
        this.userManager = userManager;
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
        this.darkFeatureManager = darkFeatureManager;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    protected abstract boolean signCallbacksWithJwt();

    protected void initBeans(AuthenticationBean authBean)
    {
        String pluginKeyPrefix = PLUGIN_KEY + "-" + authBean.getType().name().toLowerCase();
        String addonKey;
        
        this.baseBean = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withAuthentication(authBean)
                .withModule("webItems", randomWebItemBean())
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.installOnlyBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .withScopes(newHashSet(ScopeName.ADMIN))
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.installAndEnabledBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withEnabled(ENABLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.installAndDisabledBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withDisabled(DISABLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.uninstallOnlyBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.installAndUninstallBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyUtils.randomName(pluginKeyPrefix);
        this.fullLifecycleBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .withEnabled(ENABLED)
                                .withDisabled(DISABLED)
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();
    }

    @Test
    public void installUrlIsPosted() throws Exception
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        testInstallPost(true);
    }

    // with the dark feature enabled we do sign install callbacks using the new shared secret (which is useless, but the previous behaviour)
    @Test
    public void callbackSigningDarkFeaturePreventsSigningTheInstalledCallback() throws Exception
    {
        darkFeatureManager.enableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);

        try
        {
            testInstallPost(false);
        }
        finally
        {
            darkFeatureManager.disableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);
        }
    }

    private void testInstallPost(boolean signsWithPreviousJwtSharedSecret) throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        // clear the registry for this add-on
        // in case any previous installation left an "uninstalled remnant" in the registry
        // because lower down we need to test the "first install" use case
        connectAddonRegistry.removeAll(addon.getKey());

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);
            assertEquals(POST, request.getMethod());
            String firstSharedSecret = parseSharedSecret(request);
            assertEquals(signCallbacksWithJwt(), null != firstSharedSecret);
            String clientKey = parseClientKey(request);
            assertEquals(signCallbacksWithJwt() && !signsWithPreviousJwtSharedSecret, request.hasJwt()); // if signing with the *previous* secret then the first installation cannot be signed because there is no pre-shared key

            if (signCallbacksWithJwt() && !signsWithPreviousJwtSharedSecret)
            {
                JwtTestVerifier verifier = new JwtTestVerifier(firstSharedSecret, clientKey);
                assertTrue("JWT token should be signed with the shared secret in that same callback", verifier.jwtAndClientAreValid(JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX + request.getJwtToken()));
            }

            // re-install, like when the vendor posts a new descriptor on marketplace
            testFilterResults.clearRequest(addonKey, INSTALLED);
            plugin = testPluginInstaller.installAddon(addon);
            addonKey = plugin.getKey();
            request = testFilterResults.getRequest(addonKey, INSTALLED);
            String secondSharedSecret = parseSharedSecret(request);
            assertEquals(signCallbacksWithJwt(), null != secondSharedSecret);
            assertEquals(signCallbacksWithJwt(), request.hasJwt());

            if (signCallbacksWithJwt())
            {
                final String secretUsedToSignSecondInstallCallback = signsWithPreviousJwtSharedSecret ? firstSharedSecret : secondSharedSecret;
                JwtTestVerifier verifier = new JwtTestVerifier(firstSharedSecret, clientKey);
                assertTrue("JWT token should be signed with the shared secret '" + secretUsedToSignSecondInstallCallback + "'", verifier.jwtAndClientAreValid(JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX + request.getJwtToken()));

                assert firstSharedSecret != null; // just to get rid of annoying intellij warning on the line below; it doesn't parse the assertion above
                assertTrue("we should keep the shared secret on a simple update", firstSharedSecret.equals(secondSharedSecret));
            }

            // uninstall, then re-install (like a customer fixing a problem, or like a customer changing their mind)
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testPluginInstaller.uninstallAddon(plugin);
            plugin = testPluginInstaller.installAddon(addon);
            addonKey = plugin.getKey();
            request = testFilterResults.getRequest(addonKey, INSTALLED);
            String thirdSharedSecret = parseSharedSecret(request);
            assertEquals(signCallbacksWithJwt(), null != thirdSharedSecret);
            assertEquals(signCallbacksWithJwt(), request.hasJwt());

            if (signCallbacksWithJwt())
            {
                assert secondSharedSecret != null; // just to get rid of annoying intellij warning on the line below; it doesn't parse the assertion above
                assertFalse("we should issue a new shared secret on a new installation after an uninstallation", secondSharedSecret.equals(thirdSharedSecret));

                final String secretUsedToSignThirdInstallCallback = signsWithPreviousJwtSharedSecret ? secondSharedSecret : thirdSharedSecret;
                JwtTestVerifier verifier = new JwtTestVerifier(secretUsedToSignThirdInstallCallback, clientKey);
                assertTrue("JWT token should be signed with the shared secret '" + secretUsedToSignThirdInstallCallback + "'", verifier.jwtAndClientAreValid(JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX + request.getJwtToken()));
            }
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

    protected String parseClientKey(ServletRequestSnapshot request)
    {
        return new JsonParser().parse(request.getEntity()).getAsJsonObject().get(CLIENT_KEY_FIELD_NAME).getAsString();
    }

    protected String parseSharedSecret(ServletRequestSnapshot request)
    {
        return signCallbacksWithJwt() ? new JsonParser().parse(request.getEntity()).getAsJsonObject().get(SHARED_SECRET_FIELD_NAME).getAsString() : null;
    }

    @Test
    public void installRequestHasVersion() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);
            addonKey = plugin.getKey();
            final ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);

            Option<String> maybeHeader = getVersionHeader(request);
            assertVersion(maybeHeader);
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
    public void uninstallUrlIsPosted() throws Exception
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        testUninstallPost();
    }

    // the enabled and disabled callbacks have always been signed using the current secret, so we want to leave them unaffected by dark feature toggling
    @Test
    public void callbackSigningDarkFeaturePreventsSigningTheUninstalledCallback() throws IOException
    {
        darkFeatureManager.enableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);

        try
        {
            testUninstallPost();
        }
        finally
        {
            darkFeatureManager.disableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);
        }
    }

    private void testUninstallPost() throws IOException
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
            assertEquals(POST, request.getMethod());
            assertEquals(signCallbacksWithJwt(), request.hasJwt());
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
    public void uninstallRequestHasVersion() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallAddon(plugin);

            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            Option<String> maybeHeader = getVersionHeader(request);
            assertVersion(maybeHeader);
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
    public void enableRequestHasVersion() throws IOException
    {
        ConnectAddonBean addon = installAndEnabledBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.enableAddon(addonKey);

            ServletRequestSnapshot request = waitForWebhook(addonKey,ENABLED);

            Option<String> maybeHeader = getVersionHeader(request);
            assertVersion(maybeHeader);
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
    public void disableRequestHasVersion() throws IOException
    {
        ConnectAddonBean addon = installAndDisabledBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;

            testPluginInstaller.disableAddon(addonKey);

            waitForWebhook(addonKey,DISABLED);

            ServletRequestSnapshot request = testFilterResults.getRequest(finalKey, DISABLED);
            Option<String> maybeHeader = getVersionHeader(request);
            assertVersion(maybeHeader);
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void addonUserIsCreatedAndEnabled() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            assertUserExistence(addon, true);
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
    public void uninstallAddonUserIsDisabled() throws Exception
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

            assertUserExistence(addon, false);
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
    public void addonUserIsRecreatedAfterInstall() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            final boolean addonShouldHaveUser = !addon.getAuthentication().getType().equals(AuthenticationType.NONE);
            final String addonUsername = ADD_ON_USER_KEY_PREFIX + addon.getKey();
            UserProfile userProfile = userManager.getUserProfile(addonUsername);
            assertEquals("addon with auth=none should not have a user, all others should", addonShouldHaveUser, userProfile != null);

            if (addonShouldHaveUser)
            {
                applicationService.removeUser(getApplication(), ADD_ON_USER_KEY_PREFIX + addonKey);
            }

            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;

            plugin = testPluginInstaller.installAddon(addon);

            assertUserExistence(addon, addonShouldHaveUser);
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
    public void disabledAddonHadDisabledUser() throws IOException
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        testDisabledCallback();
    }

    // the enabled and disabled callbacks have always been signed using the current secret, so we want to leave them unaffected by dark feature toggling
    @Test
    public void callbackSigningDarkFeatureDoesNotAffectDisabledCallback() throws IOException
    {
        darkFeatureManager.enableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);

        try
        {
            testDisabledCallback();
        }
        finally
        {
            darkFeatureManager.disableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);
        }
    }

    private void testDisabledCallback() throws IOException
    {
        ConnectAddonBean addon = installAndDisabledBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            assertUserExistence(addon, true);
            testPluginInstaller.disableAddon(addonKey);
            ServletRequestSnapshot request = waitForWebhook(addonKey, DISABLED);
            assertUserExistence(addon, false);
            assertEquals(signCallbacksWithJwt(), request.hasJwt());
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void enabledAddonHadEnabledUser() throws Exception
    {
        assertFalse(darkFeatureManager.isFeatureEnabledForCurrentUser(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY)); // precondition
        testEnabledCallback();
    }

    // the enabled and disabled callbacks have always been signed using the current secret, so we want to leave them unaffected by dark feature toggling
    @Test
    public void callbackSigningDarkFeatureDoesNotAffectEnabledCallback() throws IOException
    {
        darkFeatureManager.enableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);

        try
        {
            testEnabledCallback();
        }
        finally
        {
            darkFeatureManager.disableFeatureForAllUsers(DARK_FEATURE_DISABLE_SIGN_INSTALL_WITH_PREV_KEY);
        }
    }

    private void testEnabledCallback() throws IOException
    {
        ConnectAddonBean addon = fullLifecycleBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            assertUserExistence(addon, true);

            testPluginInstaller.disableAddon(addonKey);

            waitForWebhook(addonKey,DISABLED);

            assertUserExistence(addon, false);

            testPluginInstaller.enableAddon(addonKey);
            ServletRequestSnapshot request = waitForWebhook(addonKey,ENABLED);

            assertUserExistence(addon, true);
            assertEquals(signCallbacksWithJwt(), request.hasJwt());
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, ENABLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    private ServletRequestSnapshot waitForWebhook(final String addonKey, final String path)
    {
        final ServletRequestSnapshot[] request = {null};

        WaitUntil.invoke(new WaitUntil.WaitCondition()
        {
            @Override
            public boolean isFinished()
            {
                request[0] = testFilterResults.getRequest(addonKey, path);
                return null != request[0];
            }

            @Override
            public String getWaitMessage()
            {
                return "waiting for enable webhook post...";
            }
        },5);

        return request[0];
    }

    private void assertVersion(Option<String> maybeHeader)
    {
        //For some reason, assertThat fails with a java.lang.LinkageError
        assertTrue("Invalid version number: " + maybeHeader.get(), isVersionNumber().matches(maybeHeader.get()));
    }

    private void assertUserExistence(ConnectAddonBean addon, boolean shouldBeActiveIfItExists)
    {
        final String username = ADD_ON_USER_KEY_PREFIX + addon.getKey();
        final UserProfile userProfile = userManager.getUserProfile(username);

        if (addon.getAuthentication().getType().equals(AuthenticationType.NONE))
        {
            assertTrue("addon with auth=none should not have a user", null == userProfile);
        }
        else
        {
            assertFalse("addon with auth!=none should have a user", null == userProfile);
            UserKey userKey = userProfile.getUserKey();
            assertEquals(String.format("addon user should%s be active", shouldBeActiveIfItExists ? "" : " not"), shouldBeActiveIfItExists, connectUserService.isUserActive(userProfile));
            assertTrue("addon user is not in group " + CONNECT_ADDON_USER_GROUP, userManager.isUserInGroup(userKey, CONNECT_ADDON_USER_GROUP));
        }
    }

    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }
}
