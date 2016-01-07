package it.com.atlassian.plugin.connect.plugin.web.condition;

import java.io.IOException;
import java.util.Collections;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.IsLicensedCondition;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.license.LicenseHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.util.TimebombedLicenseManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class LicensedConditionTest
{
    private final WebFragmentHelper webFragmentHelper;
    private final PluginAccessor pluginAccessor;
    private final TestPluginInstaller testPluginInstaller;
    private final TimebombedLicenseManager timebombedLicenseManager;
    private final TestAuthenticator testAuthenticator;

    private Condition isLicensedCondition;

    public LicensedConditionTest(LicenseHandler licenseHandler, WebFragmentHelper webFragmentHelper,
                                 PluginAccessor pluginAccessor, TestPluginInstaller testPluginInstaller,
                                 TestAuthenticator testAuthenticator)
    {
        this.webFragmentHelper = webFragmentHelper;
        this.pluginAccessor = pluginAccessor;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.timebombedLicenseManager = new TimebombedLicenseManager(licenseHandler);
    }

    private Plugin installJsonAddon(String addonKey) throws IOException
    {
        final WebItemModuleBean licenseConditionWebItem = WebItemModuleBean.newWebItemBean()
            .withKey(AddonUtil.randomPluginKey())
            .withName(new I18nProperty(AddonUtil.randomPluginKey(), "blaw"))
            .withLocation("system.nowhere")
            .withUrl("/nowhere")
            .withConditions(ImmutableList.of(
                SingleConditionBean.newSingleConditionBean().withCondition("addon_is_licensed").build()
            ))
            .build();

        ConnectAddonBean addonBean = ConnectAddonBean.newConnectAddonBean()
            .withKey(addonKey)
            .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
            .withDescription(getClass().getCanonicalName())
            .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
            .withScopes(Sets.newHashSet(ScopeName.READ))
            .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
            .withModule("webItems", licenseConditionWebItem)
            .withLicensing(true)
            .build();

        return testPluginInstaller.installAddon(addonBean);
    }

    @Before
    public void setUp() throws ConditionLoadingException, IOException
    {
        timebombedLicenseManager.setLicense();

        isLicensedCondition = webFragmentHelper.loadCondition(IsLicensedCondition.class.getName(), getConnectPlugin());
    }

    @After
    public void tearDown() throws IOException
    {
        for (String key : testPluginInstaller.getInstalledAddonKeys())
        {
            testPluginInstaller.uninstallAddon(key);
        }
    }

    private Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }

    @Test
    public void webItemsShowWhenLicensedAndAddonIsLicensedConditionPresent() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        String addonKey = timebombedLicenseManager.generateLicensedAddonKey();
        installJsonAddon(addonKey);

        isLicensedCondition.init(ImmutableMap.of("addonKey", addonKey));
        assertTrue(isLicensedCondition.shouldDisplay(Collections.emptyMap()));
    }

    @Test
    public void webItemsNotShownWhenUnlicensedAndAddonIsLicensedConditionPresent() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        String addonKey = timebombedLicenseManager.generateUnlicensedAddonKey();
        installJsonAddon(addonKey);

        isLicensedCondition.init(ImmutableMap.of("addonKey", addonKey));
        assertFalse(isLicensedCondition.shouldDisplay(Collections.emptyMap()));
    }
}
