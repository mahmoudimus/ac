package it.com.atlassian.plugin.connect.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.test.TestPluginInstaller;
import com.atlassian.plugin.connect.test.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.test.filter.ServletRequestSnaphot;
import com.atlassian.plugin.util.WaitUntil;

import org.junit.Test;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
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

    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    protected final AddonTestFilterResults testFilterResults;
    protected final ConnectApplinkManager connectApplinkManager;
    protected final ConnectAddOnUserService connectAddOnUserService;

    protected ConnectAddonBean baseBean;
    protected ConnectAddonBean installOnlyBean;
    protected ConnectAddonBean uninstallOnlyBean; // not valid for JWT tests - JWT requires an installed callback
    protected ConnectAddonBean installAndEnabledBean;
    protected ConnectAddonBean installAndDisabledBean;
    protected ConnectAddonBean installAndUninstallBean;
    protected ConnectAddonBean fullLifecycleBean;

    protected AbstractAddonLifecycleTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults testFilterResults, ConnectApplinkManager connectApplinkManager, ConnectAddOnUserService connectAddOnUserService)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddOnUserService = connectAddOnUserService;
    }

    protected void initBeans(AuthenticationBean authBean)
    {
        String pluginKeyPrefix = PLUGIN_KEY + "-" + authBean.getType().name().toLowerCase();
        String addonKey;
        
        this.baseBean = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withAuthentication(authBean)
                .build();

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
        this.installOnlyBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withInstalled(INSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
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

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
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

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
        this.uninstallOnlyBean = newConnectAddonBean(baseBean)
                .withKey(addonKey)
                .withLifecycle(
                        newLifecycleBean()
                                .withUninstalled(UNINSTALLED)
                                .build()
                )
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                .build();

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
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

        addonKey = ModuleKeyGenerator.randomName(pluginKeyPrefix);
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
            plugin = testPluginInstaller.installPlugin(addon);
            
            addonKey = plugin.getKey();
            
            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, INSTALLED);
            assertEquals(POST, request.getMethod());

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
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
            plugin = testPluginInstaller.installPlugin(addon);

            addonKey = plugin.getKey();
            
            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;

            ServletRequestSnaphot request = testFilterResults.getRequest(addonKey, UNINSTALLED);
            assertEquals(POST, request.getMethod());

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
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
            plugin = testPluginInstaller.installPlugin(addon);

            addonKey = plugin.getKey();
            
            assertTrue("addon user is not active", connectAddOnUserService.isAddOnUserActive(addonKey));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
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
            plugin = testPluginInstaller.installPlugin(addon);

            addonKey = plugin.getKey();

            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;

            assertFalse("addon user is active", connectAddOnUserService.isAddOnUserActive(addonKey));
        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
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
            plugin = testPluginInstaller.installPlugin(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;
            
            assertTrue("addon user is not active", connectAddOnUserService.isAddOnUserActive(addonKey));

            testPluginInstaller.disablePlugin(addonKey);

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
            });

            assertFalse("addon user is active", connectAddOnUserService.isAddOnUserActive(addonKey));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
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
            plugin = testPluginInstaller.installPlugin(addon);

            addonKey = plugin.getKey();
            final String finalKey = addonKey;
            
            assertTrue("addon user is not active", connectAddOnUserService.isAddOnUserActive(addonKey));

            testPluginInstaller.disablePlugin(addonKey);
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
            });

            assertFalse("addon user is active", connectAddOnUserService.isAddOnUserActive(addonKey));

            testPluginInstaller.enablePlugin(addonKey);
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
            });

            assertTrue("addon user is not active", connectAddOnUserService.isAddOnUserActive(addonKey));

        }
        finally
        {
            testFilterResults.clearRequest(addonKey, INSTALLED);
            testFilterResults.clearRequest(addonKey, ENABLED);
            testFilterResults.clearRequest(addonKey, DISABLED);
            testFilterResults.clearRequest(addonKey, UNINSTALLED);
            if (null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }
}
