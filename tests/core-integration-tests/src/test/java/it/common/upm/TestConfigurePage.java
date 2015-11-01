package it.common.upm;

import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.upm.pageobjects.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;

public class TestConfigurePage extends AbstractUpmPageTest
{

    private static final String MODULE_NAME = "configurePage";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        startConnectAddOn(MODULE_NAME, new ConfigurePageModuleMeta());
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, LINK_TEXT, "Configure", testUserFactory.admin());
    }
}
