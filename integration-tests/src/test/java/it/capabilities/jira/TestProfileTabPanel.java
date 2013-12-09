package it.capabilities.jira;

import java.rmi.RemoteException;

import com.atlassian.fugue.Option;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.RemotePage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteTabPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage2;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePageWithRemotePluginTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.google.common.base.Optional;
import it.servlet.ConnectAppServlets;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
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

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String JIRA_PROFILE_TAB_PANEL = "jira-profile-tab-panel";

    private String profileId;
    private static final String PROFILE_NAME = "2.7.1";


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

    @Before
    public void setUpTest() throws Exception
    {
//        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
//        final UserProfileControl profileClient = new UserProfileControl(jira().environmentData());
//        profileId = Long.toString(profileClient.create(new Profile().name(PROFILE_NAME + System.currentTimeMillis()).project(PROJECT_KEY)).id);
    }

    @After
    public void cleanUpTest()
    {
//        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testProfileTabPanel() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraViewProfilePage2 profilePage = jira().visit(JiraViewProfilePage2.class, Option.<String>none());
        RemoteTabPanel tabPanel = profilePage.findTabPanel("up_profile-tab-profile-tab-panel_a", Optional.<String>absent(), "profile-tab-profile-tab-panel");
        RemotePage remotePage = tabPanel.click();
        assertThat(remotePage.isLoaded(), equalTo(true));
//        final JiraViewProfilePageWithRemotePluginTab profileTabPage = jira().goTo(JiraViewProfilePageWithRemotePluginTab.class, FunctTestConstants.FRED_USERNAME);
//        JiraViewProfilePageWithRemotePluginTab page = jira().visit(
//                JiraViewProfilePageWithRemotePluginTab.class, "profile-tab-profile-tab-panel", FunctTestConstants.FRED_USERNAME);
//        Assert.assertEquals("Success", page.getMessage());

//        profileTabPage.clickRemotePluginLink();
//
//        assertThat(profileTabPage.getProfileId(), equalTo(profileId));
//        assertThat(profileTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }



}
