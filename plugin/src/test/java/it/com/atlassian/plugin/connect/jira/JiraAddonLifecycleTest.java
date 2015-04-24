package it.com.atlassian.plugin.connect.jira;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.Serializable;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraAddonLifecycleTest
{
    private static final String PLUGIN_NAME = "test-plugin";
    private static final String addonKey = "test-key";
    private static final String INSTALLED = "/installed";
    protected final TestPluginInstaller testPluginInstaller;
    protected final AddonTestFilterResults testFilterResults;
    private final GlobalPermissionManager jiraPermissionManager;
    private final I18nResolver i18nResolver;

    protected ConnectAddonBean testBean;
    private Plugin plugin = null;

    @Before
    public void setup()
    {
        this.testBean = newConnectAddonBean().withName(PLUGIN_NAME)
                                             .withScopes(Sets.newHashSet(ScopeName.ADMIN))
                                             .withModule("webItems", randomWebItemBean())
                                             .withKey(addonKey)
                                             .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(addonKey))
                                             .withLifecycle(newLifecycleBean().withInstalled(INSTALLED).build())
                                             .withAuthentication(newAuthenticationBean().withType(AuthenticationType.JWT)
                                                                                        .build())

                                             .build();
    }

    public JiraAddonLifecycleTest(GlobalPermissionManager jiraPermissionManager,
        TestPluginInstaller testPluginInstaller, AddonTestFilterResults testFilterResults, I18nResolver i18nResolver)
    {
        this.jiraPermissionManager = jiraPermissionManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testFilterResults = testFilterResults;
        this.i18nResolver = i18nResolver;
    }

    @Test
    public void testMissingAdminPermissionFailsWithCorrectError() throws IOException
    {
        try
        {
            plugin = testPluginInstaller.installAddon(testBean);
            jiraPermissionManager.removePermission(Permissions.ADMINISTER, "atlassian-addons-admin");
            plugin = testPluginInstaller.installAddon(testBean);

            fail("Addon installation should have failed");
        }
        catch (PluginInstallException e)
        {
            assertPluginInstallExceptionProperties(e, "connect.install.error.addon.admin.permission", testBean.getName());
        }
    }

    private void assertPluginInstallExceptionProperties(PluginInstallException e, String errorCode, Serializable... params)
    {
        Pair<String, Serializable[]> i18nMessageProperties = e.getI18nMessageProperties().get();
        assertThat(i18nMessageProperties.first(), equalTo(errorCode));
        assertThat(i18nMessageProperties.second(), equalTo(params));
    }

    @After
    public void cleanup() throws IOException
    {
        if(plugin != null)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
        testFilterResults.clearRequest(addonKey, INSTALLED);
        jiraPermissionManager.addPermission(Permissions.ADMINISTER, "atlassian-addons-admin");
    }
}
