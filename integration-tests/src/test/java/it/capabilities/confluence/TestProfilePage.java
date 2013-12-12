package it.capabilities.confluence;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import it.capabilities.AbstractPageTst;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of profile page in Confluence
 */
public class TestProfilePage extends AbstractPageTst
{

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn("profilePages");
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(ConfluenceUserProfilePage.class, Option.<String>none());
    }
}
