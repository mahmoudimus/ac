package it.confluence.iframe;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import it.util.ConnectTestUserFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;

import static com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean.newSpaceToolsTabBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;

/**
 * Tests for Space Tools Tab module. Note that when we refer to "Space Tools" we're referring to the post-5.0
 * Space Tools area. When we refer to "Space Admin", we're referring to the pre-5.0 (doctheme) Space Admin area.
 */
public class TestConfluenceSpaceToolsTab extends ConfluenceWebDriverTestBase
{
    public static final String TAB_MODULE_KEY = "ac-space-tab";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("spaceToolsTabs", newSpaceToolsTabBean()
                        .withName(new I18nProperty("AC Space Tab", null))
                        .withKey(TAB_MODULE_KEY)
                        .withLocation("overview")
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
        Space space = makeSpace("IBTS", "I Cant Believe Its Another Test Space");
        // Demo space uses doctheme. Templates page is in Space Admin (not to be confused with Space Operations).
        loginAndVisit(ConnectTestUserFactory.admin(product), ViewSpaceSummaryPage.class, space);
        LinkedRemoteContent addonPage = connectPageOperations.findRemoteLinkedContent(RemoteWebItem.ItemMatchingMode.LINK_TEXT, "AC Space Tab", Option.<String>none(), addonAndModuleKey(remotePlugin.getAddon().getKey(),TAB_MODULE_KEY));
        ConnectAddOnEmbeddedTestPage addonContentsPage = addonPage.click();

        assertEquals("Hello world", addonContentsPage.waitForValue("#hello-world-message"));
    }

    @Test
    public void spaceToolsShowsConnectTab()
    {
        Space space = makeSpace("ABNT", "Another Brand New Test");

        loginAndVisit(ConnectTestUserFactory.admin(product), ViewSpaceSummaryPage.class, space);

        LinkedRemoteContent addonPage = connectPageOperations.findRemoteLinkedContent(RemoteWebItem.ItemMatchingMode.LINK_TEXT, "AC Space Tab", Option.<String>none(), addonAndModuleKey(remotePlugin.getAddon().getKey(),TAB_MODULE_KEY));

        ConnectAddOnEmbeddedTestPage addonContentsPage = addonPage.click();

        assertEquals("Hello world", addonContentsPage.waitForValue("#hello-world-message"));

    }

    public Space makeSpace(String key, String name)
    {
        Space space = new Space(key, name);
        rpc.createSpace(space);
        rpc.grantAnonymousPermission(SpacePermission.VIEW, space);
        rpc.flushIndexQueue();
        return space;
    }
}
