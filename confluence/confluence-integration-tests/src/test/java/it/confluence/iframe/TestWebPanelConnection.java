
package it.confluence.iframe;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceEditPage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test web panels in web panel redirected locations works and points to the redirect servlet.
 */
public final class TestWebPanelConnection extends ConfluenceWebDriverTestBase
{
    // this is not a true redirected location but it's defined as this in reference plugin for the test purpose.
    private static final String REDIRECTED_LOCATION = "atl.editor";
    private static final String LOCATION = "atl.general";

    private static final String SPACE = "ds";
    private static final String WEB_PANEL_KEY = "test-web-panel";
    private static final ParameterCapturingServlet PARAMETER_CAPTURING_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.channelConnectionVerifyServlet());

    private static ConnectRunner remotePlugin;
    private static WebPanelModuleBean webPanel;

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
    public void webPanelInRedirectedLocationShouldPointsToRedirectServletAndDisplaysProperly() throws Exception
    {
        webPanel = newWebPanelBean()
                .withName(new I18nProperty("Editor Panel", null))
                .withKey(WEB_PANEL_KEY)
                .withLocation(REDIRECTED_LOCATION)
                .withUrl("/servlet?space_key={space.key}&page_id={content.id}")
                .build();

        remotePlugin = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addModules("webPanels", webPanel)
                .addRoute("/servlet", ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_SERVLET))
                .start();

        ConfluenceEditPage page = createAndVisitPage(ConfluenceEditPage.class);
        RemoteWebPanel panel = confluencePageOperations.findWebPanel(webPanel.getKey(remotePlugin.getAddon()));

        String iframeUrl = panel.getIFrameSourceUrl();
        assertThat(iframeUrl, containsString(RedirectServletPath.forModule(remotePlugin.getAddon().getKey(), "test-web-panel")));

        Map<String, String> params = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(params.get("space_key"), is(SPACE));
        assertThat(params.get("page_id"), is(page.getPageId()));
    }

    @Test
    public void webPanelWithAddOnBaseUrlWithPathShouldConnectionWithConnectJs() throws Exception
    {
        webPanel = newWebPanelBean()
                .withName(new I18nProperty("Panel in redirected location", null))
                .withKey("test-web-panel")
                .withUrl("/servlet")
                .withLocation(LOCATION)
                .build();

        remotePlugin = new ConnectRunner(product)
                .setBaseUrlPath("/path")
                .setAuthenticationToNone()
                .addModules("webPanels", webPanel)
                .addRoute("/servlet", ConnectAppServlets.wrapContextAwareServlet(ConnectAppServlets.channelConnectionVerifyServlet()))
                .start();

        createAndVisitPage(ConfluenceViewPage.class);

        // Check if iframe connected with connect JS. The "channel-connected-message" element is displayed after connection is established.
        connectPageOperations.findWebPanel(webPanel.getKey(remotePlugin.getAddon())).waitUntilContentElementNotEmpty("channel-connected-message");
    }

    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(testUserFactory.basicUser()), SPACE, "Page with webpanel", "some page content");
        return product.visit(pageClass, pageData.getId());
    }
}

