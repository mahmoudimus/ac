package it.capabilities.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class TestConfluenceWebPanel extends ConfluenceWebDriverTestBase
{
    private static final String IFRAME_URL_EDIT = "http://edit.example.com";
    private static final String IFRAME_URL_VIEW = "http://view.example.com";
    private static final String IFRAME_URL_PROFILE = "http://profile.example.com";

    private static final int IFRAME_EDIT_HEIGHT = 200;
    private static final int IFRAME_VIEW_HEIGHT = 50;
    private static final int IFRAME_PROFILE_HEIGHT = 100;

    private static final String SPACE = "ds";

    private static ConnectCapabilitiesRunner remotePlugin;
    private static WebPanelCapabilityBean editorWebPanel;
    private static WebPanelCapabilityBean viewWebPanel;
    private static WebPanelCapabilityBean profileWebPanel;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        editorWebPanel = WebPanelCapabilityBean.newWebPanelBean()
                .withName(new I18nProperty("Editor Panel", "editor-panel"))
                .withLocation("atl.editor")
                .withUrl(IFRAME_URL_EDIT)
                .withLayout(new WebPanelLayout("100%", IFRAME_EDIT_HEIGHT + "px"))
                .withWeight(1)
                .build();

        viewWebPanel = WebPanelCapabilityBean.newWebPanelBean()
                .withName(new I18nProperty("View Panel", "view-panel"))
                .withLocation("atl.general")
                .withUrl(IFRAME_URL_VIEW)
                .withLayout(new WebPanelLayout("100%", IFRAME_VIEW_HEIGHT + "px"))
                .withWeight(1)
                .build();

        profileWebPanel = WebPanelCapabilityBean.newWebPanelBean()
                .withName(new I18nProperty("Profile Panel", "profile-panel"))
                .withLocation("atl.userprofile")
                .withUrl(IFRAME_URL_PROFILE)
                .withLayout(new WebPanelLayout("100%", IFRAME_PROFILE_HEIGHT + "px"))
                .withWeight(1)
                .build();

        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapability(editorWebPanel)
                .addCapability(viewWebPanel)
                .addCapability(profileWebPanel)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
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
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(IFRAME_URL_EDIT)); // will end with the plugin's displayUrl and auth parameters
    }

    @Test
    public void iFrameHeightIsCorrectOnEditPage() throws Exception
    {
        RemoteWebPanel webPanel = findEditPageWebPanel();
        assertThat(webPanel.getIFrame().getSize().getHeight(), is(IFRAME_EDIT_HEIGHT));
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
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(IFRAME_URL_VIEW)); // will end with the plugin's displayUrl and auth parameters
    }

    @Test
    public void iFrameHeightIsCorrectOnViewPage() throws Exception
    {
        RemoteWebPanel webPanel = findViewPageWebPanel();
        assertThat(webPanel.getIFrame().getSize().getHeight(), is(IFRAME_VIEW_HEIGHT));
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
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(IFRAME_URL_PROFILE)); // will end with the plugin's displayUrl and auth parameters
    }

    @Test
    public void iFrameHeightIsCorrectOnProfilePage() throws Exception
    {
        RemoteWebPanel webPanel = findProfilePageWebPanel();
        assertThat(webPanel.getIFrame().getSize().getHeight(), is(IFRAME_PROFILE_HEIGHT));
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
        final ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "Page with webpanel", "some page content");
    }

}
