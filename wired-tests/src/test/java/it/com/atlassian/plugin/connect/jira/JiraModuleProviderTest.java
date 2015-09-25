package it.com.atlassian.plugin.connect.jira;

import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static it.com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraModuleProviderTest extends AbstractConnectAddonTest
{
    private final TestPluginInstaller testPluginInstaller;
    
    public JiraModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
        this.testPluginInstaller = testPluginInstaller;
    }
    
    @Test
    public void goodThingsHappen() throws IOException
    {
        String json = readAddonTestFile("descriptorWithPluginProvidedModule.json");
        testPluginInstaller.installAddon(json);
    }
}
