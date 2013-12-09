package it.capabilities.jira;

import java.rmi.RemoteException;

import com.atlassian.fugue.Option;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteTabPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.google.common.base.Optional;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote profile tab panel in JIRA
 */
public class TestProfileTabPanel extends TestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(jira().getProductInstance().getBaseUrl(), "my-plugin")
                .addCapability(ConnectTabPanelModuleProvider.PROFILE_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Profile Tab Panel", null))
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
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraViewProfilePage profilePage = jira().visit(JiraViewProfilePage.class, Option.<String>none());
        RemoteTabPanel tabPanel = profilePage.findTabPanel("up_profile-tab-profile-tab-panel_a", Optional.<String>absent(), "profile-tab-profile-tab-panel");
        RemotePluginEmbeddedTestPage remotePage = tabPanel.click();
        assertThat(remotePage.isLoaded(), equalTo(true));
        assertThat(remotePage.getMessage(), equalTo("Success"));
    }



}
