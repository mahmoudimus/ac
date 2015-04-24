package it.confluence.iframe;

import com.atlassian.confluence.it.Space;
import com.atlassian.confluence.it.SpacePermission;
import com.atlassian.confluence.it.admin.BundledTheme;
import com.atlassian.confluence.pageobjects.page.admin.templates.SpaceTemplatesPage;
import com.atlassian.confluence.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.confluence.capabilities.provider.SpaceToolsTabModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import com.google.common.base.Supplier;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean.newSpaceToolsTabBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;

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
        Space space = makeSpace(RandomStringUtils.randomAlphanumeric(4).toLowerCase(), "spaceAdminShowsConnectTabDocTheme", true);

        // Demo space uses doctheme. Templates page is in Space Admin (not to be confused with Space Operations).
        loginAndVisit(testUserFactory.admin(), SpaceTemplatesPage.class, space.getKey());

        String pageKey = ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), TAB_MODULE_KEY);
        String webItemId = pageKey + SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX;
        LinkedRemoteContent addonPage = connectPageOperations.findTabPanel(webItemId, Option.<String>none(), pageKey);

        final RemoteWebPanel addonContentsPage = addonPage.click(
                    RemoteWebPanel.class,
                    ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), TAB_MODULE_KEY)
        );

        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return addonContentsPage.containsHelloWorld();
            }
        }));
    }

    @Test
    public void spaceToolsShowsConnectTab()
    {
        Space space = makeSpace(RandomStringUtils.randomAlphanumeric(4).toLowerCase(), "spaceToolsShowsConnectTab", false);

        loginAndVisit(testUserFactory.admin(), ViewSpaceSummaryPage.class, space);

        LinkedRemoteContent addonPage = connectPageOperations.findRemoteLinkedContent(RemoteWebItem.ItemMatchingMode.LINK_TEXT, "AC Space Tab", Option.<String>none(), addonAndModuleKey(remotePlugin.getAddon().getKey(), TAB_MODULE_KEY));

        final RemoteWebPanel addonContentsPage = addonPage.click(
                RemoteWebPanel.class,
                ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), TAB_MODULE_KEY)
        );

        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return addonContentsPage.containsHelloWorld();
            }
        }));
    }

    public Space makeSpace(String key, String name, boolean docTheme)
    {
        Space space = new Space(key, name);
        rpc.createSpace(space);
        rpc.grantAnonymousPermission(SpacePermission.VIEW, space);
        rpc.flushIndexQueue();

        if (docTheme)
        {
            rpc.setThemeForSpace(space, BundledTheme.DOCUMENTATION);
        }

        return space;
    }

}
