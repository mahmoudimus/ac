package it.com.atlassian.plugin.connect.installer;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserService;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

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

    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    protected final AddonTestFilterResults testFilterResults;
    protected final ConnectApplinkManager connectApplinkManager;
    protected final ConnectAddOnUserService connectAddOnUserService;
    private final UserManager userManager;
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;

    protected ConnectAddonBean baseBean;
    protected ConnectAddonBean installOnlyBean;
    protected ConnectAddonBean uninstallOnlyBean; // not valid for JWT tests - JWT requires an installed callback
    protected ConnectAddonBean installAndEnabledBean;
    protected ConnectAddonBean installAndDisabledBean;
    protected ConnectAddonBean installAndUninstallBean;
    protected ConnectAddonBean fullLifecycleBean;

    protected AbstractAddonLifecycleTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults testFilterResults, ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService, UserManager userManager, ApplicationService applicationService, ApplicationManager applicationManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
        this.userManager = userManager;
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }

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
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);
            assertEquals(POST, request.getMethod());

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
    public void installRequestHasVersion() throws Exception
    {
        ConnectAddonBean addon = installOnlyBean;

        Plugin plugin = null;
        String addonKey = null;
        try
        {
            plugin = testPluginInstaller.installAddon(addon);
            
            addonKey = plugin.getKey();
            
            ServletRequestSnapshot request = testFilterResults.getRequest(addonKey, INSTALLED);
            String version = request.getHeaders().get("Atlassian-Connect-Version");
            assertNotNull(StringUtils.trimToNull(version));

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
    public void uninstallUrlIsPosted() throws Exception
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
            assertEquals(POST, request.getMethod());

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
    public void uninstallRequestHasVersion() throws Exception
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
            String version = request.getHeaders().get("Atlassian-Connect-Version");
            assertNotNull(StringUtils.trimToNull(version));
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
                testPluginInstaller.uninstallJsonAddon(plugin);
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

            testPluginInstaller.uninstallJsonAddon(plugin);
            plugin = null;

            assertUserExistence(addon, false);
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
    public void addonUserIsRecreatedAfterInstall() throws Exception
    {
        ConnectAddonBean addon = installAndUninstallBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();

            final boolean addOnShouldHaveUser = !addon.getAuthentication().getType().equals(AuthenticationType.NONE);
            assertEquals("addon with auth=none should not have a user, all others should", addOnShouldHaveUser, connectAddOnUserService.isAddOnUserActive(addonKey));

            if (addOnShouldHaveUser)
            {
                applicationService.removeUser(getApplication(), ADD_ON_USER_KEY_PREFIX + addonKey);
            }

            testPluginInstaller.uninstallJsonAddon(plugin);
            plugin = null;

            plugin = testPluginInstaller.installAddon(addon);

            assertUserExistence(addon, addOnShouldHaveUser);
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
    public void disabledAddonHadDisabledUser() throws Exception
    {
        ConnectAddonBean addon = installAndDisabledBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;

            assertUserExistence(addon, true);
            testPluginInstaller.disableAddon(addonKey);

            WaitUntil.invoke(new WaitUntil.WaitCondition()
            {
                @Override
                public boolean isFinished()
                {
                    return null != testFilterResults.getRequest(finalKey, DISABLED);
                }

                @Override
                public String getWaitMessage()
                {
                    return "waiting for disable webhook post...";
                }
            }, 5);

            assertUserExistence(addon, false);
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
    }

    @Test
    public void enabledAddonHadEnabledUser() throws Exception
    {
        ConnectAddonBean addon = fullLifecycleBean;

        Plugin plugin = null;
        String addonKey = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;

            assertUserExistence(addon, true);

            testPluginInstaller.disableAddon(addonKey);
            WaitUntil.invoke(new WaitUntil.WaitCondition()
            {
                @Override
                public boolean isFinished()
                {
                    return null != testFilterResults.getRequest(finalKey, DISABLED);
                }

                @Override
                public String getWaitMessage()
                {
                    return "waiting for disable webhook post...";
                }
            },5);

            assertUserExistence(addon, false);

            testPluginInstaller.enableAddon(addonKey);
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

            assertUserExistence(addon, true);
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, ENABLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
            }
        }
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
            assertEquals(String.format("addon user should%s be active", shouldBeActiveIfItExists ? "" : " not"), shouldBeActiveIfItExists, connectAddOnUserService.isAddOnUserActive(addon.getKey()));
            assertTrue("addon user is not in group " + CONNECT_ADDON_USER_GROUP, userManager.isUserInGroup(userKey, CONNECT_ADDON_USER_GROUP));
        }
    }

    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }
}
