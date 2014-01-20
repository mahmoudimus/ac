package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.page.admin.templates.SpaceTemplatesPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.SpaceToolsTabModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedSpaceTemplatesPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for Space Tools Tab module. Note that when we refer to "Space Tools" we're referring to the post-5.0
 * Space Tools area. When we refer to "Space Admin", we're referring to the pre-5.0 (doctheme) Space Admin area.
 */
public class TestConfluenceSpaceToolsTab extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapabilities("spaceToolsTabs", newPageBean()
                        .withName(new I18nProperty("AC Space Tab", null))
                        .withKey("ac-space-tab")
                        .withLocation("contenttools")
                        .withWeight(1)
                        .withUrl("/pg")
                        .build()
                )
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void spaceAdminShowsConnectTab()
    {
        loginAsAdmin();

        // Demo space uses doctheme. Templates page is in Space Admin (not to be confused with Space Operations).
        SpaceTemplatesPage page = product.visit(FixedSpaceTemplatesPage.class, "ds");

        LinkedRemoteContent addonPage = connectPageOperations.findTabPanel("ac-space-tab" + SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX, Option.<String>none(), "ac-space-tab");

        RemotePluginEmbeddedTestPage addonContentsPage = addonPage.click();

        assertThat(addonContentsPage.isLoaded(), equalTo(true));
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));
    }

    @Test
    public void spaceToolsShowsConnectTab()
    {
        loginAsAdmin();

        SpaceTemplatesPage page = product.visit(FixedSpaceTemplatesPage.class, "ts");

        LinkedRemoteContent addonPage = connectPageOperations.findRemoteLinkedContent(RemoteWebItem.ItemMatchingMode.LINK_TEXT, "AC Space Tab", Option.<String>none(), "ac-space-tab");

        RemotePluginEmbeddedTestPage addonContentsPage = addonPage.click();

        assertThat(addonContentsPage.isLoaded(), equalTo(true));
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));

    }
}
