package it.capabilities.jira;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class WebPanelTest extends JiraWebDriverTestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    private RemoteWebPanel webPanel;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability(newWebPanelBean()
                        .withName(new I18nProperty("HipChat Discussions", "hipchat.discussions"))
                        .withUrl("http://www.google.com")
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
            remotePlugin.stop();
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
        assertThat(webPanel.getIFrameSourceUrl(), startsWith("http://www.google.com")); // will end with the plugin's displayUrl and auth parameters
    }
}
