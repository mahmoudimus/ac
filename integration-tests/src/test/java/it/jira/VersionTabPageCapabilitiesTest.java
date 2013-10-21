package it.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPanelBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote version tab panel in JIRA
 */
public class VersionTabPageCapabilitiesTest extends TestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String JIRA_VERSION_TAB_PANEL = "jira-version-tab-panel";

    private String versionId;
    private static final String VERSION_NAME = "2.7.1";


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(jira().getProductInstance().getBaseUrl(),"my-plugin")
                .addOAuth()
                .addCapability(newVersionTabPanelBean()
                        .withKey(JIRA_VERSION_TAB_PANEL)
                        .withName(new I18nProperty("Version Tab Panel", "my.versiontabpanel"))
                        .withUrl("/ipp?version_id=${version.id}&project_id=${project.id}&project_key=${project.key}")
                        .withWeight(1234)
                        .build())
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
    public void setUpTest() throws Exception
    {
        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
        final VersionClient versionClient = new VersionClient(jira().environmentData());
        versionId = Long.toString(versionClient.create(new Version().name(VERSION_NAME + System.currentTimeMillis()).project(PROJECT_KEY)).id);
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testVersionTabPanel() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, "jira-version-tab");

        versionTabPage.clickTab();

        assertThat(versionTabPage.getVersionId(), equalTo(versionId));
        assertThat(versionTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }
}
