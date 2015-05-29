package com.atlassian.plugin.connect.testsupport.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.sal.api.user.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static java.util.Arrays.asList;

public class ScopeTestHelperImpl implements ScopeTestHelper
{
    private static final Logger log = LoggerFactory.getLogger(ScopeTestHelperImpl.class);
    private static final UserKey USER_KEY = null;

    private final TestPluginInstaller testPluginInstaller;

    public ScopeTestHelperImpl(TestPluginInstaller testPluginInstaller) throws IOException
    {
        this.testPluginInstaller = testPluginInstaller;
    }

    @Override
    public Map<ScopeName, Plugin> installScopedAddOns() throws IOException
    {
        final Map<ScopeName, Plugin> installedPlugins = new HashMap<>();
        for (ScopeName scopeName : ScopeName.values())
        {
            ConnectAddonBean addOnBean = createAddOnBeanWithScope(scopeName);
            final Plugin addOn = testPluginInstaller.installAddon(addOnBean);
            installedPlugins.put(scopeName, addOn);
        }

        ConnectAddonBean addOnBean = createAddOnBeanWithScope(null);
        final Plugin addOn = testPluginInstaller.installAddon(addOnBean);
        installedPlugins.put(null, addOn);

        return installedPlugins;
    }

    @Override
    public void uninstallScopedAddOns(Map<ScopeName, Plugin> installedPlugins)
    {
        for (Plugin plugin : installedPlugins.values())
        {
            try
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
            catch (IOException e)
            {
                log.error(String.format("Unable to uninstall add-on '%s'", plugin.getKey()), e);
            }
        }
    }

    private String getPluginKeyForScopeName(final ScopeName scopeName)
    {
        if (scopeName == null)
        {
            return "NO_SCOPE" + '-' + System.currentTimeMillis();
        }
        return scopeName.toString() + '-' + System.currentTimeMillis();
    }

    private ConnectAddonBean createAddOnBeanWithScope(ScopeName scopeName)
    {
        final String key = getPluginKeyForScopeName(scopeName);
        ConnectAddonBeanBuilder connectAddonBeanBuilder = newConnectAddonBean()
                .withKey(key)
                .withName(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withLicensing(true)
                .withAuthentication(newAuthenticationBean()
                        .withType(AuthenticationType.JWT)
                        .build())
                .withLifecycle(newLifecycleBean()
                        .withInstalled("/installed")
                        .build())
                .withModule("generalPages", newPageBean()
                        .withUrl("/hello-world.html")
                        .withKey("general")
                        .withName(new I18nProperty("Greeting", "greeting"))
                        .build());

        // scopes are optional so that we can have "no scopes" test classes
        if (null != scopeName)
        {
            connectAddonBeanBuilder = connectAddonBeanBuilder.withScopes(new HashSet<>(asList(scopeName)));
        }

        return connectAddonBeanBuilder.build();
    }
}
