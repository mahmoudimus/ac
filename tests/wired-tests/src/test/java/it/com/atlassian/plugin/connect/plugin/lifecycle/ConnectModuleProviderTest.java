package it.com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.upm.spi.PluginControlHandler;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectModuleProviderTest extends AbstractConnectAddonTest
{

    private final PluginControlHandler pluginControlHandler;

    private Plugin addon;

    public ConnectModuleProviderTest(WebItemModuleProvider webItemModuleProvider,
            TestPluginInstaller testPluginInstaller,
            TestAuthenticator testAuthenticator,
            PluginControlHandler pluginControlHandler)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
        this.pluginControlHandler = pluginControlHandler;
    }

    @After
    public void tearDown() throws IOException
    {
        if (addon != null)
        {
            testPluginInstaller.uninstallAddon(addon);
        }
    }

    @Test
    public void shouldInstallAddonWithPluginProvidedModule() throws IOException
    {
        String json = readAddonTestFile("descriptorWithPluginProvidedModule.json");
        addon = testPluginInstaller.installAddon(json);

        assertHasModuleDescriptor("my-plugin-with-provided-module", "test-provided-module");
    }

    private void assertHasModuleDescriptor(String addonKey, String moduleKey)
    {
        Plugin addon = pluginControlHandler.getPlugin(addonKey);
        Collection<ModuleDescriptor<?>> moduleDescriptors = addon.getModuleDescriptors();
        String completeModuleKey = "com.atlassian.plugins.atlassian-connect-reference-plugin:" + addonAndModuleKey(addonKey, moduleKey);
        assertThat(moduleDescriptors, hasItem(new ModuleDescriptorHasCompleteKey(completeModuleKey)));
    }

    private static class ModuleDescriptorHasCompleteKey extends TypeSafeMatcher<ModuleDescriptor>
    {

        private String completeModuleKey;

        public ModuleDescriptorHasCompleteKey(String completeModuleKey)
        {
            this.completeModuleKey = completeModuleKey;
        }

        @Override
        protected boolean matchesSafely(ModuleDescriptor moduleDescriptor)
        {
            return completeModuleKey.equals(moduleDescriptor.getCompleteKey());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("plugin module descriptor with complete key " + completeModuleKey);
        }
    }
}
