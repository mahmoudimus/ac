package it.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.ComponentTabPageModuleDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraComponentTabPage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.ComponentTabPageModule;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote component tab panel in JIRA
 */
@XmlDescriptor
public class TestComponentTabPage extends TestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String MODULE_KEY = "jira-component-tab-panel";
    private static final String ACTUAL_MODULE_KEY = ComponentTabPageModuleDescriptor.COMPONENT_TAB_PAGE_MODULE_PREFIX + MODULE_KEY;

    private String componentId;
    private static final String COMPONENT_NAME = "test-component";


    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(jira().environmentData().getBaseUrl().toString())
                .addOAuth()
                .add(ComponentTabPageModule.key(MODULE_KEY)
                        .name("Component Tab Panel")
                        .path("/ipp?component_id=${component.id}&project_id=${project.id}&project_key=${project.key}")
                        .resource(ConnectAppServlets.apRequestServlet()))
                .start();
    }

    @AfterClass
    public static void tearDownClassTest() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
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
        JiraComponentTabPage componentTabPage = jira().goTo(JiraComponentTabPage.class, PROJECT_KEY, componentId, remotePlugin.getPluginKey(), ACTUAL_MODULE_KEY);

        componentTabPage.clickTab();

        assertThat(componentTabPage.getComponentId(), equalTo(componentId));
        assertThat(componentTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }

}
