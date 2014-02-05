package it.com.atlassian.plugin.connect.event;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.AddonTestFilter;
import it.com.atlassian.plugin.connect.AddonTestFilterResults;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.TestPluginInstaller;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectEventHandlerOAuthTest
{
    public static final String PLUGIN_KEY = "my-oauth-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final AddonTestFilterResults testFilterResults;
    

    public ConnectEventHandlerOAuthTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, AddonTestFilterResults addonTestFilterResults)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.testFilterResults = addonTestFilterResults;
    }

    @BeforeClass
    public void setup()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception
    {
        
        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(PLUGIN_KEY))
                .withLifecycle(
                        newLifecycleBean()
                                .withUninstalled("/uninstalled")
                                .build()
                )
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);
            
            testPluginInstaller.uninstallPlugin(plugin);
            plugin = null;
            
            HttpServletRequest request = testFilterResults.getRequest(PLUGIN_KEY, "/uninstalled");
            assertNotNull(request);
            
        }
        finally
        {
            testFilterResults.clearRequest(PLUGIN_KEY, "/uninstalled");
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }

}
