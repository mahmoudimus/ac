package it.capabilities;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.upm.pageobjects.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of addon configure page in Confluence
 */
public class TestConfigurePage extends AbstractPageTst
{
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("configurePage");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Configure"));
    }

    @Override
    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
        // hmmm not pretty
        ((PluginManager)page).expandPluginRow(PLUGIN_KEY);
    }
}
