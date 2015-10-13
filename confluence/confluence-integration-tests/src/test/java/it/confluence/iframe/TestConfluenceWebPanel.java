package it.confluence.iframe;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceEditPage;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceUserProfilePage;
import com.atlassian.connect.test.jira.pageobjects.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ToggleableConditionServlet;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

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
    public static void startConnectAddOn() throws Exception
    {
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
                .withConditions(ToggleableConditionServlet.toggleableConditionBean())
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
        login(testUserFactory.basicUser());
    }

    @Test
    public void webPanelExistsOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        Assert.assertThat(webPanel, CoreMatchers.is(CoreMatchers.not(CoreMatchers.nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSourceUrl(), CoreMatchers.containsString(IFRAME_URL_EDIT));
    }

    @Test
    public void iFrameParametersAreCorrectOnEditPage() throws Exception
    {
        ConfluenceEditPage editPage = createAndVisitPage(ConfluenceEditPage.class);
        RemoteWebPanel webPanel = connectPageOperations.findWebPanel(editorWebPanel.getKey(remotePlugin.getAddon()));
        Assert.assertThat(webPanel.getSpaceKey(), CoreMatchers.is(SPACE));
        Assert.assertThat(webPanel.getPageId(), CoreMatchers.is(editPage.getPageId()));
        Assert.assertThat(webPanel.getContentId(), CoreMatchers.is(editPage.getPageId()));
    }

    @Test
    public void iFrameHeightIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getHeight(), CoreMatchers.is(IFRAME_EDIT_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getWidth(), CoreMatchers.is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel().waitUntilContentLoaded();
        Assert.assertThat(webPanel.getCustomMessage(), CoreMatchers.is(IFRAME_CONTENT_EDIT));
    }

    @Test
    public void webPanelExistsOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        Assert.assertThat(webPanel, CoreMatchers.is(CoreMatchers.not(CoreMatchers.nullValue())));
    }

    @Test
    public void webPanelIsNotAccessibleWithFalseCondition() throws Exception
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);
        createAndVisitPage(ConfluenceViewPage.class); // revisit the view page now that condition has been set to false
        Assert.assertThat(connectPageOperations.existsWebPanel(viewWebPanel.getKey(remotePlugin.getAddon())), CoreMatchers.is(false));
    }

    @Test
    public void iFrameUrlIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSourceUrl(), CoreMatchers.containsString(IFRAME_URL_VIEW));
    }

    @Test
    public void iFrameParametersAreCorrectOnViewPage() throws Exception
    {
        ConfluenceViewPage viewPage = createAndVisitPage(ConfluenceViewPage.class);
        RemoteWebPanel webPanel = connectPageOperations.findWebPanel(viewWebPanel.getKey(remotePlugin.getAddon()));
        Assert.assertThat(webPanel.getSpaceKey(), CoreMatchers.is(SPACE));
        Assert.assertThat(webPanel.getPageId(), CoreMatchers.is(viewPage.getPageId()));
        Assert.assertThat(webPanel.getContentId(), CoreMatchers.is(viewPage.getPageId()));
    }

    @Test
    public void iFrameHeightIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getHeight(), CoreMatchers.is(IFRAME_VIEW_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getWidth(), CoreMatchers.is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel().waitUntilContentLoaded();
        Assert.assertThat(webPanel.getCustomMessage(), CoreMatchers.is(IFRAME_CONTENT_VIEW));
    }

    @Test
    public void webPanelExistsOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        Assert.assertThat(webPanel, CoreMatchers.is(CoreMatchers.not(CoreMatchers.nullValue())));
    }

    @Test
    public void iFrameUrlIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        Assert.assertThat(webPanel.getIFrameSourceUrl(), CoreMatchers.containsString(IFRAME_URL_PROFILE));
    }

    @Test
    public void iFrameHeightIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getHeight(), CoreMatchers.is(IFRAME_PROFILE_HEIGHT));
    }

    @Test
    public void iFrameWidthIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        Assert.assertThat(webPanel.getIFrameSize().getWidth(), CoreMatchers.is(IFRAME_WIDTH));
    }

    @Test
    public void iFrameContentIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel().waitUntilContentLoaded();
        Assert.assertThat(webPanel.getCustomMessage(), CoreMatchers.is(IFRAME_CONTENT_PROFILE));
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
        return confluenceOps.setPage(Option.some(testUserFactory.basicUser()), SPACE, "Page with webpanel", "some page content");
    }

    private static String px(int px)
    {
        return px + "px";
    }

}
