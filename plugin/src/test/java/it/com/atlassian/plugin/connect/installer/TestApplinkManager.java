package it.com.atlassian.plugin.connect.installer;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import static org.junit.Assert.assertNotNull;

//TODO: add wire test harness facillities and write actual tests. This can happen over time. JD will work on this during his vacation
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
