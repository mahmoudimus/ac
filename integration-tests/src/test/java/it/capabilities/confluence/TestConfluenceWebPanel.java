package it.capabilities.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class TestConfluenceWebPanel extends ConfluenceWebDriverTestBase
{
    private static final String WEB_PANELS = "webPanels";

    private static final String IFRAME_URL_EDIT = "/tcwp-edit";
    private static final String IFRAME_URL_VIEW = "/tcwp-view";
    private static final String IFRAME_URL_PROFILE = "/tcwp-profile";

    private static final String IFRAME_URL_PARAMETERS = "?page_id={page.id}&space_key={space.key}";

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

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        editorWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("Editor Panel", "editor-panel"))
                .withKey("editor-panel")
                .withLocation("atl.editor")
                .withUrl(IFRAME_URL_EDIT + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_EDIT_HEIGHT)))
                .withWeight(1)
                .build();

        viewWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("View Panel", "view-panel"))
                .withKey("view-panel")
                .withLocation("atl.general")
                .withUrl(IFRAME_URL_VIEW + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_VIEW_HEIGHT)))
                .withWeight(1)
                .build();

        profileWebPanel = WebPanelModuleBean.newWebPanelBean()
                .withName(new I18nProperty("Profile Panel", "profile-panel"))
                .withKey("profile-panel")
                .withLocation("atl.userprofile")
                .withUrl(IFRAME_URL_PROFILE + IFRAME_URL_PARAMETERS)
                .withLayout(new WebPanelLayout(px(IFRAME_WIDTH), px(IFRAME_PROFILE_HEIGHT)))
                .withWeight(1)
                .build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "cwp-plugin")
                .addModule(WEB_PANELS, editorWebPanel)
                .addModule(WEB_PANELS, viewWebPanel)
                .addModule(WEB_PANELS, profileWebPanel)
                .addRoute(IFRAME_URL_EDIT, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_EDIT))
                .addRoute(IFRAME_URL_VIEW, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_VIEW))
                .addRoute(IFRAME_URL_PROFILE, ConnectAppServlets.customMessageServlet(IFRAME_CONTENT_PROFILE))
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

    @Before
    public void beforeEachTest()
    {
        loginAsAdmin();
    }

    @Test
    public void webPanelExistsOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel.getIFrameSourceUrl(), containsString(IFRAME_URL_EDIT));
    }

    @Test
    public void iFrameParametersAreCorrectOnEditPage() throws Exception
    {
        ConfluenceEditPage editPage = createAndVisitPage(ConfluenceEditPage.class);
        RemoteWebPanel webPanel = editPage.findWebPanel(editorWebPanel.getKey());
        assertThat(webPanel.getSpaceKey(), is(SPACE));
        assertThat(webPanel.getPageId(), is(editPage.getPageId()));
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
    public void iFrameUrlIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel.getIFrameSourceUrl(), containsString(IFRAME_URL_VIEW));
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
        ConfluenceEditPage editPage = createAndVisitPage(ConfluenceEditPage.class);
        return editPage.findWebPanel(editorWebPanel.getKey());
    }

    private RemoteWebPanel findViewPageWebPanel() throws Exception
    {
        ConfluenceViewPage viewPage = createAndVisitPage(ConfluenceViewPage.class);
        return viewPage.findWebPanel(viewWebPanel.getKey());
    }

    private RemoteWebPanel findProfilePageWebPanel() throws Exception
    {
        ConfluenceUserProfilePage profilePage = product.visit(ConfluenceUserProfilePage.class);
        return profilePage.findWebPanel(profileWebPanel.getKey());
    }

    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "Page with webpanel", "some page content");
    }

    private static String px(int px)
    {
        return px + "px";
    }

}
