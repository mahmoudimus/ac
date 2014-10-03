package it.com.atlassian.plugin.connect.installer;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.test.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.upm.spi.PluginInstallException;
import com.google.common.collect.Sets;

@RunWith(AtlassianPluginsTestRunner.class)
public class JiraAddonLifecycleTest
{
    private static final String PLUGIN_NAME = "test-plugin";
    private static final String addonKey = "test-key";
    private static final String INSTALLED = "/installed";
    protected final TestPluginInstaller testPluginInstaller;
    protected final AddonTestFilterResults testFilterResults;
    private final GlobalPermissionManager jiraPermissionManager;

    protected ConnectAddonBean testBean;

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
        TestPluginInstaller testPluginInstaller, AddonTestFilterResults testFilterResults)
    {
        this.jiraPermissionManager = jiraPermissionManager;
        this.testPluginInstaller = testPluginInstaller;
        this.testFilterResults = testFilterResults;
    }

    @Test
    public void testMissingAdminPermissionFailsWithCorrectError() throws IOException
    {
        Plugin plugin = null;
        try
        {
            plugin = testPluginInstaller.installAddon(testBean);
            jiraPermissionManager.removePermission(Permissions.ADMINISTER, "atlassian-addons-admin");
            plugin = testPluginInstaller.installAddon(testBean);
            
            fail("Addon installation should have failed");
        }
        catch (Exception e)
        {
            assertTrue((e instanceof PluginInstallException));
            assertEquals("connect.install.error.addon.admin.permission", ((PluginInstallException) e).getCode().get());
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallJsonAddon(plugin);
                testFilterResults.clearRequest(addonKey, INSTALLED);
                jiraPermissionManager.addPermission(Permissions.ADMINISTER, "atlassian-addons-admin");
            }
        }
    }
}
