package it.com.atlassian.plugin.connect.jira.web;

import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static it.com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertEquals;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraWebPanelBlacklistTest {
    private final TestPluginInstaller testPluginInstaller;

    public JiraWebPanelBlacklistTest(TestPluginInstaller testPluginInstaller) {
        this.testPluginInstaller = testPluginInstaller;
    }

    @Test
    public void shouldNotInstallPluginWithInvalidWebPanelLocations() throws IOException {
        String json = readAddonTestFile("jiraDescriptorWithInvalidLocation.json");
        try {
            testPluginInstaller.installAddon(json);
        } catch (InvalidDescriptorException e) {
            assertEquals("Installation failed. The add-on includes a web fragment with an unsupported location ([atl.header.after.scripts]).", e.getMessage());
        }
    }

}
