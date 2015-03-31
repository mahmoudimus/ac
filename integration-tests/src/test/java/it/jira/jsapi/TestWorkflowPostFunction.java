package it.jira.jsapi;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAddWorkflowTransitionFunctionParamsPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraWorkflowTransitionPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static org.junit.Assert.assertEquals;

public class TestWorkflowPostFunction extends JiraWebDriverTestBase
{
    private static ConnectRunner runner;
    private static final String WORKFLOW_POST_FUNCTION_NAME = "My Connect Post Function";
    private static final String WORKFLOW_POST_FUNCTION_PAGE = "ac-workflow-post-function";
    private static final String WORKFLOW_POST_FUNCTION_INVALID_NAME = "My Invalid Connect Post Function";
    private static final String WORKFLOW_POST_FUNCTION_INVALID_PAGE = "ac-workflow-invalid-post-function";
    private static final String WORKFLOW_NAME = "classic default workflow";
    private static final String WORKFLOW_STEP = "1";
    private static final String WORKFLOW_TRANSITION = "5";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModules("jiraWorkflowPostFunctions",
                        newWorkflowPostFunctionBean()
                            .withName(new I18nProperty(WORKFLOW_POST_FUNCTION_NAME, null))
                            .withKey(WORKFLOW_POST_FUNCTION_PAGE)
                            .withView(new UrlBean("/wpf-view?config={postFunction.config}"))
                            .withEdit(new UrlBean("/wpf-edit?config={postFunction.config}"))
                            .withCreate(new UrlBean("/wpf-create"))
                            .withTriggered(new UrlBean("/wpf-triggered"))
                            .withDescription(new I18nProperty("workflow post function description", null))
                            .build(),
                        newWorkflowPostFunctionBean()
                                .withName(new I18nProperty(WORKFLOW_POST_FUNCTION_INVALID_NAME, null))
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
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testCreateWorkflowPostFunction()
    {
        JiraWorkflowTransitionPage workflowTransitionPage = loginAndVisit(TestUser.ADMIN,
                JiraWorkflowTransitionPage.class, "live", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION).createOrEditDraft();
        JiraAddWorkflowTransitionFunctionParamsPage addonPage = workflowTransitionPage.addPostFunction(
                "my-plugin", WORKFLOW_POST_FUNCTION_PAGE, WORKFLOW_POST_FUNCTION_NAME);
        addonPage.submitWorkflowParams();

        JiraAddWorkflowTransitionFunctionParamsPage postFunction = workflowTransitionPage.updatePostFunction(
                "my-plugin", WORKFLOW_POST_FUNCTION_PAGE);
        String workflowConfiguration = postFunction.getIframeQueryParams().get("config");
        assertEquals("workflow configuration text", workflowConfiguration);
    }

    @Test
    public void testCreateInvalidWorkflowPostFunction()
    {
        JiraWorkflowTransitionPage workflowTransitionPage = loginAndVisit(TestUser.ADMIN,
                JiraWorkflowTransitionPage.class, "live", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION).createOrEditDraft();
        JiraAddWorkflowTransitionFunctionParamsPage addonPage = workflowTransitionPage.addPostFunction(
                "my-plugin", WORKFLOW_POST_FUNCTION_INVALID_PAGE, WORKFLOW_POST_FUNCTION_INVALID_NAME);

        String url = product.getTester().getDriver().getCurrentUrl();
        addonPage.submitWorkflowParams();
        assertEquals(url, product.getTester().getDriver().getCurrentUrl());
    }
}
