package it.com.atlassian.plugin.connect.plugin.blacklist;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

@RunWith(AtlassianPluginsTestRunner.class)
public class WebFragmentLocationBlacklistTest
{
    private final WebFragmentLocationBlacklist webFragmentLocationBlacklist;

    @Autowired
    public WebFragmentLocationBlacklistTest(WebFragmentLocationBlacklist webFragmentLocationBlacklist)
    {
        this.webFragmentLocationBlacklist = webFragmentLocationBlacklist;
    }

    @Test
    public void testShouldReturnBlacklistedWebItems() throws Exception
    {
        assertTrue(webFragmentLocationBlacklist.getBlacklistedWebItemLocations().contains("atl.header.webitem.blacklisted"));
    }

    @Test
    public void testShouldReturnBlacklistedWebPanels() throws Exception
    {
        assertTrue(webFragmentLocationBlacklist.getBlacklistedWebPanelLocations().contains("atl.header.webpanel.blacklisted"));
    }

}
