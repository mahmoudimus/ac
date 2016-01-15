package it.confluence.jsapi;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.confluence.pageobjects.page.content.EditContentPage;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.confluence.pageobjects.page.user.ViewProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.RemoteNavigatorGeneralPage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.util.concurrent.Promise;
import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.servlet.ConfluenceAppServlets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class TestNavigator extends ConfluenceWebDriverTestBase
{
    /*
     These tests work by navigating to a page and then attempting to bind a page object to that page.
     If the navigation itself fails, the page will not bind successfully and the test will fail.
     */

    private static List<Exception> setupFailure = new ArrayList<>();
    private static final String PAGE_KEY = "ac-navigator-general-page";
    private static final String WEB_PANEL_KEY = "ac-navigator-editor-web-panel";
    private static final String WEB_ITEM_KEY = "ac-navigator-web-item";
    private static final int IFRAME_VIEW_HEIGHT = 50;
    private static final int IFRAME_WIDTH = 300;
    private static ConnectRunner remotePlugin;

    private static Promise<Content> createdPage;
    private static Space space;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        String spaceKey = "NAVTEST" + System.currentTimeMillis();
        space = restClient.spaces().create(Space.builder().key(spaceKey).name("Nav Space").build(), false).get();
        createdPage = restClient.content().create(Content.builder(ContentType.PAGE)
                .space(space)
                .body("<p>Page content</p>", ContentRepresentation.STORAGE)
                .title("Page")
                .build());

        try
        {
            remotePlugin = new ConnectRunner(ConfluenceWebDriverTestBase.product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                    .setAuthenticationToNone()
                    .addModules("generalPages",
                            newPageBean()
                                    .withName(new I18nProperty("Nvg", null))
                                    .withUrl("/nvg")
                                    .withKey(PAGE_KEY)
                                    .withLocation("system.header/left")
                                    .build()
                    )
                    .addModule("webItems",
                            newWebItemBean()
                                    .withName(new I18nProperty("Context", null))
                                    .withUrl("/nvg-context")
                                    .withKey(WEB_ITEM_KEY)
                                    .withLocation("system.header/left")
                                    .withTarget(newWebItemTargetBean()
                                            .withType(WebItemTargetType.dialog)
                                            .build())
                                    .build()
                    )
                    .addModule("webPanels",
                            newWebPanelBean()
                                    .withName(new I18nProperty("Editor Web Panel", null))
                                    .withUrl("/nvg-web-panel")
                                    .withKey(WEB_PANEL_KEY)
                                    .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_VIEW_HEIGHT)))
                                    .withWeight(1)
                                    .withLocation("atl.editor")
                                    .build()
                    )
                    .addRoute("/nvg", ConfluenceAppServlets.navigatorServlet(createdPage.get().getId().asLong(), space.getKey()))
                    .addRoute("/nvg-web-panel", ConfluenceAppServlets.navigatorContextServlet())
                    .addRoute("/nvg-context", ConfluenceAppServlets.navigatorContextServlet())
                    .start();
        }
        catch (Exception ex)
        {
            setupFailure.add(ex);
        }

    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        if (!setupFailure.isEmpty())
        {
            throw setupFailure.get(0);
        }
    }

    @Test
    public void testNavigateToDashboard() throws Exception
    {
        DashboardPage dashboard = loginAndClickToNavigate("navigate-to-dashboard", DashboardPage.class);
        assertNotNull(dashboard.getDashboardPanel());
    }

    @Test
    public void testNavigateToPage() throws Exception
    {
        ViewPage viewpage = loginAndClickToNavigate("navigate-to-page", ViewPage.class, createdPage.get().getId().serialise());
        long idFromPageObject = viewpage.getPageId();
        assertEquals(createdPage.get().getId().asLong(), idFromPageObject);
    }

    @Test
    public void testNavigateToEditPage() throws Exception
    {
        com.atlassian.confluence.it.Page page = new com.atlassian.confluence.it.Page(createdPage.get().getId().asLong());
        EditContentPage editContentPage = loginAndClickToNavigate("navigate-to-edit-page", EditContentPage.class, page);
        String titleFromPageObject = editContentPage.getTitle();
        assertEquals(createdPage.get().getTitle(), titleFromPageObject);
    }

    @Test
    public void testNavigateToUserProfile() throws Exception
    {
        ViewProfilePage viewProfilePage = loginAndClickToNavigate("navigate-to-user-profile", ViewProfilePage.class, "admin");
        boolean hasProfileTitle = viewProfilePage.hasTitle().byDefaultTimeout();
        assertTrue(hasProfileTitle);
    }

    @Test
    public void testNavigateToSpaceTools() throws Exception
    {
        com.atlassian.confluence.it.Space space = new com.atlassian.confluence.it.Space(TestNavigator.space.getKey(), TestNavigator.space.getKey());
        ViewSpaceSummaryPage viewSpaceSummaryPage = loginAndClickToNavigate("navigate-to-space-tools", ViewSpaceSummaryPage.class, space);
        // there is nothing on the view spaceSummaryPage that is appropriate to assert
        // so this test really does nothing until we can convert to STR (https://ecosystem.atlassian.net/browse/ACDEV-2081)
    }

    @Test
    public void testGetCurrentContextOfEditPage() throws Exception
    {
        com.atlassian.confluence.it.Page page = new com.atlassian.confluence.it.Page(createdPage.get().getId().asLong());
        EditContentPage editContentPage = loginAndClickToNavigate("navigate-to-edit-page", EditContentPage.class, page);

        // this web panel contains an API call to get the current page context, then inserts it into a div.
        RemoteWebPanel webPanel = connectPageOperations.findWebPanel(WEB_PANEL_KEY);
        String pageContext = webPanel.getIFrameElement("ac-current-page-context");
    }

    public <P extends com.atlassian.pageobjects.Page> P loginAndClickToNavigate(String id, java.lang.Class<P> aPageClass, Object... args)
    {
        RemoteNavigatorGeneralPage page = loginAndVisit(ConfluenceWebDriverTestBase.testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);

        return page.clickToNavigate(id, aPageClass, args);
    }

    private static String px(int px)
    {
        return px + "px";
    }
}
