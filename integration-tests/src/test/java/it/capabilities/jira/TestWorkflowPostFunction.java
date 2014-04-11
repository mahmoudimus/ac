package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAddWorkflowTransitionFunctionParamsPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraWorkflowTransitionPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;

import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestWorkflowPostFunction extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private static final String WORKFLOW_POST_FUNCTION_PAGE = "ac-workflow-post-function";
    private static final String WORKFLOW_POST_FUNCTION_INVALID_PAGE = "ac-workflow-invalid-post-function";
    private static final String WORKFLOW_NAME = "classic default workflow";
    private static final Integer WORKFLOW_STEP = 1;
    private static final Integer WORKFLOW_TRANSITION = 5;

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModules("jiraWorkflowPostFunctions",
                        newWorkflowPostFunctionBean()
                            .withName(new I18nProperty("My function", null))
                            .withKey(WORKFLOW_POST_FUNCTION_PAGE)
                            .withView(new UrlBean("/wpf-view"))
                            .withEdit(new UrlBean("/wpf-edit"))
                            .withCreate(new UrlBean("/wpf-create"))
                            .withTriggered(new UrlBean("/wpf-triggered"))
                            .withDescription(new I18nProperty("workflow post function description", null))
                            .build(),
                        newWorkflowPostFunctionBean()
                                .withName(new I18nProperty("My invalid function", null))
                                .withKey(WORKFLOW_POST_FUNCTION_INVALID_PAGE)
                                .withView(new UrlBean("/wpf-view"))
                                .withEdit(new UrlBean("/wpf-edit"))
                                .withCreate(new UrlBean("/wpf-invalid-create"))
                                .withTriggered(new UrlBean("/wpf-triggered"))
                                .withDescription(new I18nProperty("workflow post function description", null))
                                .build()
                        )
                .addRoute("/wpf-view", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-edit", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-create", ConnectAppServlets.workflowPostFunctionServlet())
                .addRoute("/wpf-invalid-create", ConnectAppServlets.failValidateWorkflowPostFunctionServlet())
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
    public void testCreateWorkflowPostFunction()
    {
        loginAsAdmin();
        JiraWorkflowTransitionPage workflowTransitionPage = product.visit(JiraWorkflowTransitionPage.class, "live", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION).createOrEditDraft();
        JiraAddWorkflowTransitionFunctionParamsPage addonPage = workflowTransitionPage.addPostFunction("my-plugin", WORKFLOW_POST_FUNCTION_PAGE);
        assertThat(addonPage.isLoaded(), equalTo(true));
        addonPage.submitWorkflowParams();
        String workflowConfiguration = workflowTransitionPage.workflowPostFunctionConfigurationValue(WORKFLOW_POST_FUNCTION_PAGE);
        assertEquals("workflow configuration text", workflowConfiguration);
    }


    @Test
    public void testCreateInvalidWorkflowPostFunction()
    {
        loginAsAdmin();
        JiraWorkflowTransitionPage workflowTransitionPage = product.visit(JiraWorkflowTransitionPage.class, "live", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION).createOrEditDraft();
        JiraAddWorkflowTransitionFunctionParamsPage addonPage = workflowTransitionPage.addPostFunction("my-plugin", WORKFLOW_POST_FUNCTION_INVALID_PAGE);
        assertThat(addonPage.isLoaded(), equalTo(true));

        String url = product.getTester().getDriver().getCurrentUrl();
        addonPage.submitWorkflowParams();
        assertEquals(url, product.getTester().getDriver().getCurrentUrl());
    }


}
