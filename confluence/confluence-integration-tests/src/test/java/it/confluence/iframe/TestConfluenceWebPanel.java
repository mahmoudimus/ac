package it.confluence.iframe;

import java.net.MalformedURLException;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceEditPage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceUserProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.confluence.ConfluenceWebDriverTestBase;
import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class TestConfluenceWebPanel extends ConfluenceWebDriverTestBase
{
    private static final String WEB_PANELS = "webPanels";

    private static final String IFRAME_URL_EDIT = "/tcwp-edit";
    private static final String IFRAME_URL_VIEW = "/tcwp-view";
    private static final String IFRAME_URL_PROFILE = "/tcwp-profile";

    private static final String IFRAME_URL_PARAMETERS = "?page_id={page.id}&space_key={space.key}&content_id={content.id}";

    private static final String IFRAME_CONTENT_EDIT = "edit contents";
    private static final String IFRAME_CONTENT_VIEW = "view contents";
    private static final String IFRAME_CONTENT_PROFILE = "profile contents";

    private static final int IFRAME_EDIT_HEIGHT = 200;
    private static final int IFRAME_VIEW_HEIGHT = 50;
    private static final int IFRAME_PROFILE_HEIGHT = 100;
    private static final int IFRAME_WIDTH = 300;

    private static final String SPACE = "ds";

    private static ConnectRunner remotePlugin;
    private static WebPanelModuleBean editorWebPanel;
    private static WebPanelModuleBean viewWebPanel;
    private static WebPanelModuleBean profileWebPanel;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        // iframe url and params are checked by it.confluence.iframe.TestRedirectedWebPanel.webPanelInRedirectedLocationShouldPointsToRedirectServletAndDisplaysProperly the
        editorWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("Editor Panel", null))
                .withKey("editor-panel")
                .withLocation("atl.editor")
                .withUrl(IFRAME_URL_EDIT + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_EDIT_HEIGHT)))
                .withWeight(1)
                .build();

        viewWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("View Panel", null))
                .withKey("view-panel")
                .withLocation("atl.general")
                .withUrl(IFRAME_URL_VIEW + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_VIEW_HEIGHT)))
                .withConditions(toggleableConditionBean())
                .withWeight(1)
                .build();

        profileWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("Profile Panel", null))
                .withKey("profile-panel")
                .withLocation("atl.userprofile")
                .withUrl(IFRAME_URL_PROFILE + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_PROFILE_HEIGHT)))
                .withWeight(1)
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "cwp-plugin")
                .setAuthenticationToNone()
                .addModules(WEB_PANELS, editorWebPanel, viewWebPanel, profileWebPanel)
                .addRoute(IFRAME_URL_EDIT, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_EDIT, false))
                .addRoute(IFRAME_URL_VIEW, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_VIEW, false))
                .addRoute(IFRAME_URL_PROFILE, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_PROFILE, false))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void beforeEachTest()
    {
        login(testUserFactory.basicUser());
    }

    @Test
    public void webPanelExistsOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void iFrameHeightIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel.getIFrameSize().getHeight(), is(IFRAME_EDIT_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel.getIFrameSize().getWidth(), is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel().waitUntilContentLoaded();
        assertThat(webPanel.getCustomMessage(), is(IFRAME_CONTENT_EDIT));
    }

    @Test
    public void webPanelExistsOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void webPanelIsNotAccessibleWithFalseCondition() throws Exception
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);
        createAndVisitPage(ConfluenceViewPage.class); // revisit the view page now that condition has been set to false
        assertThat(connectPageOperations.existsWebPanel(viewWebPanel.getKey(remotePlugin.getAddon())), is(false));
    }

    @Test
    public void iFrameUrlIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel.getIFrameSourceUrl(), containsString(IFRAME_URL_VIEW));
    }

    @Test
    public void iFrameParametersAreCorrectOnViewPage() throws Exception
    {
        ConfluenceViewPage viewPage = createAndVisitPage(ConfluenceViewPage.class);
        RemoteWebPanel webPanel = connectPageOperations.findWebPanel(viewWebPanel.getKey(remotePlugin.getAddon()));
        assertThat(webPanel.getSpaceKey(), is(SPACE));
        assertThat(webPanel.getPageId(), is(viewPage.getPageId()));
        assertThat(webPanel.getContentId(), is(viewPage.getPageId()));
    }

    @Test
    public void iFrameHeightIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel.getIFrameSize().getHeight(), is(IFRAME_VIEW_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel.getIFrameSize().getWidth(), is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel().waitUntilContentLoaded();
        assertThat(webPanel.getCustomMessage(), is(IFRAME_CONTENT_VIEW));
    }

    @Test
    public void webPanelExistsOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        assertThat(webPanel.getIFrameSourceUrl(), containsString(IFRAME_URL_PROFILE));
    }

    @Test
    public void iFrameHeightIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        assertThat(webPanel.getIFrameSize().getHeight(), is(IFRAME_PROFILE_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        assertThat(webPanel.getIFrameSize().getWidth(), is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel().waitUntilContentLoaded();
        assertThat(webPanel.getCustomMessage(), is(IFRAME_CONTENT_PROFILE));
    }

    private RemoteWebPanel findEditPageWebPanel() throws Exception
    {
        createAndVisitPage(ConfluenceEditPage.class);
        return connectPageOperations.findWebPanel(editorWebPanel.getKey(remotePlugin.getAddon()));
    }

    private RemoteWebPanel findViewPageWebPanel() throws Exception
    {
        createAndVisitPage(ConfluenceViewPage.class);
        return connectPageOperations.findWebPanel(viewWebPanel.getKey(remotePlugin.getAddon()));
    }

    private RemoteWebPanel findProfilePageWebPanel() throws Exception
    {
        product.visit(ConfluenceUserProfilePage.class);
        return connectPageOperations.findWebPanel(profileWebPanel.getKey(remotePlugin.getAddon()));
    }

    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(testUserFactory.basicUser()), SPACE, "Page with webpanel", "some page content");
    }

    private static String px(int px)
    {
        return px + "px";
    }

}
