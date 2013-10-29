package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraComponentTabPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean.newComponentTabPanelBean;
import static com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner.newMustacheServlet;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote component tab panel in JIRA
 */
public class TestComponentTabPanel extends TestBase
{
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String COMPONENT_NAME = "test-component";
    private static final String JIRA_COMPONENT_TAB_PANEL = "jira-component-tab-panel";
    private static ConnectCapabilitiesRunner remotePlugin;

    private String componentId;


    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(jira().getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability("componentTabPanels",newComponentTabPanelBean()
                        .withName(new I18nProperty("Component Tab Panel", null))
                        .withUrl("/ipp?component_id=${component.id}&project_id=${project.id}&project_key=${project.key}")
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", newMustacheServlet("iframe.mu"))
                .start();
    }

    @AfterClass
    public static void tearDownClassTest() throws Exception
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
        final ComponentClient componentClient = new ComponentClient(jira().environmentData());
        componentId = Long.toString(componentClient.create(new Component().name(COMPONENT_NAME + System.currentTimeMillis()).project(PROJECT_KEY)).id);
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }


    @Test
    public void testComponentTabPanel() throws Exception
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraComponentTabPage componentTabPage = jira().goTo(JiraComponentTabPage.class, PROJECT_KEY, componentId, "component-tab");

        componentTabPage.clickTab();

        assertThat(componentTabPage.getComponentId(), equalTo(componentId));
        assertThat(componentTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }

}
