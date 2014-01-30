package it.capabilities.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote profile tab panel in JIRA
 */
public class TestProfileTabPanel extends ConnectWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addModule("jiraProfileTabPanels",
                        newTabPanelBean()
                                .withName(new I18nProperty("Profile Tab Panel", null))
                                .withKey("profile-tab-panel")
                                .withUrl("/myProfileAddon")
                                .withWeight(1234)
                                .build())
                .addRoute("/myProfileAddon", ConnectAppServlets.apRequestServlet())
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

    @Test
    public void testProfileTabPanel() throws RemoteException
    {
        loginAsAdmin();
        product.visit(ViewProfilePage.class);
        LinkedRemoteContent tabPanel = connectPageOperations.findTabPanel("up_profile-tab-profile-tab-panel_a",
                Option.<String>none(), "profile-tab-panel");
        RemotePluginEmbeddedTestPage remotePage = tabPanel.click();
        assertThat(remotePage.isLoaded(), equalTo(true));
        assertThat(remotePage.getMessage(), equalTo("Success"));
    }



}
