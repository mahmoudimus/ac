
package it.jira.iframe;

import com.atlassian.connect.test.jira.pageobjects.JiraProjectAdministrationPage;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingServlet;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test web panels in movable locations works and points to the redirect servlet.
 */
public final class TestWebPanelInMovableLocations extends JiraWebDriverTestBase
{
    private static final String WEB_PANEL = "test-web-panel";

    // this is not a true movable location but it's defined as movable in reference plugin for the test purpose.
    private static final String MOVABLE_LOCATION = "atl.jira.proj.config.sidebar";

    private static final ParameterCapturingServlet PARAMETER_CAPTURING_SERVLET = ConnectAppServlets.parameterCapturingServlet(ConnectAppServlets.channelConnectionVerifyServlet());

    private static ConnectRunner runner;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        product.quickLoginAsAdmin();

        runner = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addModules(
                        "webPanels",
                        newWebPanelBean()
                                .withName(new I18nProperty("Panel in movable location", null))
                                .withKey(WEB_PANEL)
                                .withUrl("/servlet?project_key={project.key}&project_id={project.id}")
                                .withLocation(MOVABLE_LOCATION)
                                .build()
                )
                .addRoute("/servlet", ConnectAppServlets.wrapContextAwareServlet(PARAMETER_CAPTURING_SERVLET))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void webPanelInMovableLocationShouldPointsToRedirectServletAndDisplaysProperly()
    {
        JiraProjectAdministrationPage page = product.visit(JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanel panel = page.findWebPanel(getModuleKey(runner, WEB_PANEL)).waitUntilContentElementNotEmpty("channel-connected-message");

        String iframeUrl = panel.getIFrameSourceUrl();
        assertThat(iframeUrl, containsString(RedirectServletPath.forModule(runner.getAddon().getKey(), WEB_PANEL)));

        Map<String, String> params = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(params.get("project_id"), is(project.getId()));
        assertThat(params.get("project_key"), is(project.getKey()));
    }

    private String getModuleKey(ConnectRunner runner, String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }
}

