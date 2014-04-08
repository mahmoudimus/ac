package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import org.junit.*;
import org.junit.rules.TestRule;

import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class TestWebPanel extends JiraWebDriverTestBase
{
    private static final String WEB_PANEL_KEY = "hip-chat-discussions";
    
    private static ConnectRunner remotePlugin;
    
    private String webPanelModuleKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(),RemotePluginUtils.randomPluginKey())
                .setAuthenticationToNone()
                .addModule("webPanels", newWebPanelBean()
                        .withName(new I18nProperty("HipChat Discussions", "hipchat.discussions"))
                        .withKey(WEB_PANEL_KEY)
                        // panel doesn't load properly as it 404s - not a prob for this test (asserts existence not content)
                        .withUrl("/myWebPanelPage?issueId{issue.id}")
                        .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                        .withLayout(new WebPanelLayout("100%", "200px"))
                        .withWeight(1234)
                        .withConditions(toggleableConditionBean())
                        .build()).start();
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

        this.webPanelModuleKey = addonAndModuleKey(remotePlugin.getAddon().getKey(),WEB_PANEL_KEY);
    }

    @Test
    public void webPanelExists()
    {
        JiraViewProjectPage viewProjectPage = visitViewProjectPage();
        assertThat(viewProjectPage.findWebPanel(webPanelModuleKey), is(not(nullValue())));
    }

    @Test
    public void panelIsNotVisibleWithFalseCondition()
    {
        visitViewProjectPage();
        assertThat("AddOn web panel should be present", connectPageOperations.existsWebPanel(webPanelModuleKey), is(true));
        remotePlugin.setToggleableConditionShouldDisplay(false);
        visitViewProjectPage();
        assertThat("AddOn web panel should NOT be present", connectPageOperations.existsWebPanel(webPanelModuleKey), is(false));
    }

    @Test
    public void urlIsCorrect()
    {
        JiraViewProjectPage viewProjectPage = visitViewProjectPage();
        RemoteWebPanel webPanel = viewProjectPage.findWebPanel(webPanelModuleKey);
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(remotePlugin.getAddon().getBaseUrl() + "/myWebPanelPage"));
    }

    private JiraViewProjectPage visitViewProjectPage()
    {
        return product.visit(JiraViewProjectPage.class, project.getKey());
    }
}
