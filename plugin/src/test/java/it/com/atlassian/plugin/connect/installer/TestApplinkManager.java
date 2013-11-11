package it.com.atlassian.plugin.connect.installer;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.plugin.connect.plugin.installer.ConnectApplinkManager;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(AtlassianPluginsTestRunner.class)
public class TestApplinkManager
{
    private final ConnectApplinkManager applinkManager;

    public TestApplinkManager(ConnectApplinkManager applinkManager)
    {
        this.applinkManager = applinkManager;
    }

    @Test
    public void testName() throws Exception
    {

        assertNotNull(applinkManager);
    }
}
