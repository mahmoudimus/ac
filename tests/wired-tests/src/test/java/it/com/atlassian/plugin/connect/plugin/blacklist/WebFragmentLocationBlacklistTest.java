package it.com.atlassian.plugin.connect.plugin.blacklist;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertThat;

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
        assertThat(webFragmentLocationBlacklist.blacklistedWebItemLocations(), Matchers.contains("atl.header.webitem.blacklisted"));
    }

    @Test
    public void testShouldReturnBlacklistedWebPanels() throws Exception
    {
        assertThat(webFragmentLocationBlacklist.blacklistedWebItemLocations(), Matchers.contains("atl.header.webpanel.blacklisted"));
    }
}
