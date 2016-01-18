package it.com.atlassian.plugin.connect.plugin.web.condition;

import java.io.IOException;
import java.util.Collections;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.util.TimebombedLicenseManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

@RunWith(AtlassianPluginsTestRunner.class)
public class LicensedConditionTest
{
    public static final String LOCATION = "fake-addon-location";
    public static final String ADDON_MODULE_KEY = "test-addon-module-key";

    private final TestPluginInstaller testPluginInstaller;
    private final TimebombedLicenseManager timebombedLicenseManager;
    private final TestAuthenticator testAuthenticator;
    private final WebInterfaceManager webInterfaceManager;

    public LicensedConditionTest(TimebombedLicenseManager timebombedLicenseManager,
                                 TestPluginInstaller testPluginInstaller,
                                 TestAuthenticator testAuthenticator, WebInterfaceManager webInterfaceManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.timebombedLicenseManager = timebombedLicenseManager;
        this.webInterfaceManager = webInterfaceManager;
    }

    private Plugin installJsonAddon(String addonKey) throws IOException
    {
        final WebItemModuleBean licenseConditionWebItem = WebItemModuleBean.newWebItemBean()
            .withKey(ADDON_MODULE_KEY)
            .withName(new I18nProperty(ADDON_MODULE_KEY, "blaw"))
            .withLocation(LOCATION)
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
    public void webItemsDisplayedWhenLicensedAndAddonIsLicensedConditionPresent() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        String addonKey = timebombedLicenseManager.generateLicensedAddonKey();
        final Plugin plugin = installJsonAddon(addonKey);

        final Iterable<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());
        assertThat(displayableWebItems, contains(webItemWithKey(plugin, ADDON_MODULE_KEY)));
    }

    @Test
    public void webItemsNotDisplayedWhenUnlicensedAndAddonIsLicensedConditionPresent() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        String addonKey = timebombedLicenseManager.generateUnlicensedAddonKey();
        final Plugin plugin = installJsonAddon(addonKey);

        final Iterable<WebItemModuleDescriptor> displayableWebItems = webInterfaceManager.getDisplayableItems(LOCATION, Collections.emptyMap());
        assertThat(displayableWebItems, not(contains(webItemWithKey(plugin, ADDON_MODULE_KEY))));
    }

    private Matcher<WebItemModuleDescriptor> webItemWithKey(Plugin plugin, String key)
    {
        return new TypeSafeMatcher<WebItemModuleDescriptor>()
        {

            @Override
            protected boolean matchesSafely(WebItemModuleDescriptor item)
            {
                return getWebItemKey(item).equals(getWebItemModuleKey());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("web item with key ");
                description.appendValue(getWebItemModuleKey());
            }

            @Override
            protected void describeMismatchSafely(WebItemModuleDescriptor item, Description mismatchDescription)
            {
                mismatchDescription.appendText("web item with key ");
                mismatchDescription.appendValue(getWebItemKey(item));
            }

            private String getWebItemKey(WebItemModuleDescriptor item)
            {
                return item.getKey();
            }

            private String getWebItemModuleKey()
            {
                return ModuleKeyUtils.addonAndModuleKey(plugin.getKey(), key);
            }
        };
    }
}
