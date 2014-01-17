package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class TestWebPanel extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    private RemoteWebPanel webPanel;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addModule("webPanels", newWebPanelBean()
                        .withName(new I18nProperty("HipChat Discussions", "hipchat.discussions"))
                        .withKey("hip-chat-discussions")
                        // panel doesn't load properly as it 404s - not a prob for this test (asserts existence not content)
                        .withUrl("/myWebPanelPage?issueId{issue.id}")
                        .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                        .withLayout(new WebPanelLayout("100%", "200px"))
                        .withWeight(1234)
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
        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        webPanel = viewProjectPage.findWebPanel("hip-chat-discussions");
    }

    @Test
    public void webPanelExists()
    {
        assertThat(webPanel, is(not(nullValue())));
    }

    @Test
    public void urlIsCorrect()
    {
        assertThat(webPanel.getIFrameSourceUrl(), startsWith(remotePlugin.getAddon().getBaseUrl() + "/myWebPanelPage"));
    }
}
