package it.common.upm;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.upm.pageobjects.InstalledPluginDetails;
import com.atlassian.upm.pageobjects.Link;
import com.atlassian.upm.pageobjects.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Test of addon configure page in Confluence
 */
public class TestConfigurePage extends AbstractUpmPageTest
{
    private static final String MODULE_NAME = "configurePage";
    
    @Inject
    PageBinder pageBinder;
    
    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn(MODULE_NAME);
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Configure"), testUserFactory.admin());
    }

    @Override
    protected String getModuleType()
    {
        return MODULE_NAME;
    }

    @Override
    protected Link getLink(InstalledPluginDetails installedPluginDetails)
    {
        return installedPluginDetails.getConfigureLink();
    }
}
