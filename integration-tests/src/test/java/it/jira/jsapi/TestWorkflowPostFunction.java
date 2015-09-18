package it.jira.jsapi;

import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionFunctionParamsPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionPostFunctionPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.workflow.ExtendedViewWorkflowTransitionPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.workflow.JiraEditWorkflowTransitionFunctionParamsPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;

public class TestWorkflowPostFunction extends JiraWebDriverTestBase
{
    private static final String WORKFLOW_POST_FUNCTION_NAME = "My Connect Post Function";
    private static final String WORKFLOW_POST_FUNCTION_KEY = "ac-workflow-post-function";
    private static final String WORKFLOW_POST_FUNCTION_INVALID_NAME = "My Invalid Connect Post Function";
    private static final String WORKFLOW_POST_FUNCTION_INVALID_KEY = "ac-workflow-invalid-post-function";

    private static ConnectRunner runner;

    private static String addonKey;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        addonKey = AddonTestUtils.randomAddOnKey();
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModules("jiraWorkflowPostFunctions",
                        newWorkflowPostFunctionBean()
                            .withName(new I18nProperty(WORKFLOW_POST_FUNCTION_NAME, null))
                            .withKey(WORKFLOW_POST_FUNCTION_KEY)
                            .withView(new UrlBean("/wpf-view?config={postFunction.config}"))
                            .withEdit(new UrlBean("/wpf-edit?config={postFunction.config}"))
                            .withCreate(new UrlBean("/wpf-create"))
                            .withTriggered(new UrlBean("/wpf-triggered"))
                            .withDescription(new I18nProperty("workflow post function description", null))
                            .build(),
                        newWorkflowPostFunctionBean()
                                .withName(new I18nProperty(WORKFLOW_POST_FUNCTION_INVALID_NAME, null))
                                .withKey(WORKFLOW_POST_FUNCTION_INVALID_KEY)
                                .withView(new UrlBean("/wpf-view"))
                                .withEdit(new UrlBean("/wpf-edit"))
                                .withCreate(new UrlBean("/wpf-invalid-create"))
                                .withTriggered(new UrlBean("/wpf-triggered"))
                                .withDescription(new I18nProperty("workflow post function description", null))
                                .build()
                        )
                .addModuleMeta(new WorkflowPostFunctionModuleMeta())
                .addRoute("/wpf-view", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-edit", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-create", ConnectAppServlets.workflowPostFunctionServlet())
                .addRoute("/wpf-invalid-create", ConnectAppServlets.failValidateWorkflowPostFunctionServlet())
                .start();

        ensureDefaultWorkflowActivated();
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
        String clonedWorkflowName = RandomStringUtils.randomAlphabetic(10);
        product.backdoor().getTestkit().workflow().cloneWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME, clonedWorkflowName);

        login(testUserFactory.admin());

        ViewWorkflowSteps workflowStepsPage = product.visit(ViewWorkflowSteps.class, clonedWorkflowName);
        ExtendedViewWorkflowTransitionPage viewWorkflowTransitionPage = (ExtendedViewWorkflowTransitionPage)goToFirstTransition(clonedWorkflowName, workflowStepsPage);

        AddWorkflowTransitionPostFunctionPage addTransitionPostFunctionPage = viewWorkflowTransitionPage.goToAddPostFunction();

        AddWorkflowTransitionFunctionParamsPage addTransitionPostFunctionParamsPage
                = addTransitionPostFunctionPage.selectAndSubmitByName(WORKFLOW_POST_FUNCTION_NAME);

        connectPageOperations.findWebPanel(addonAndModuleKey(addonKey, WORKFLOW_POST_FUNCTION_KEY));
        viewWorkflowTransitionPage = (ExtendedViewWorkflowTransitionPage)addTransitionPostFunctionParamsPage.submit();

        JiraEditWorkflowTransitionFunctionParamsPage editTransitionFunctionParamsPage
                = viewWorkflowTransitionPage.updateFirstPostFunction(addonKey, WORKFLOW_POST_FUNCTION_KEY);
        String workflowConfiguration = editTransitionFunctionParamsPage.getIframeQueryParams().get("config");

        assertEquals("workflow configuration text for post function", workflowConfiguration);
    }

    @Test
    public void testCreateInvalidWorkflowPostFunction()
    {
        String clonedWorkflowName = RandomStringUtils.randomAlphabetic(10);
        product.backdoor().getTestkit().workflow().cloneWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME, clonedWorkflowName);

        login(testUserFactory.admin());

        ViewWorkflowSteps workflowStepsPage = product.visit(ViewWorkflowSteps.class, clonedWorkflowName);
        ExtendedViewWorkflowTransitionPage viewWorkflowTransitionPage = (ExtendedViewWorkflowTransitionPage)goToFirstTransition(clonedWorkflowName, workflowStepsPage);

        JiraEditWorkflowTransitionFunctionParamsPage addTransitionPostFunctionPage
                = viewWorkflowTransitionPage.goToAddAddonPostFunction(
                WORKFLOW_POST_FUNCTION_INVALID_NAME, addonKey, WORKFLOW_POST_FUNCTION_INVALID_KEY);

        String url = product.getTester().getDriver().getCurrentUrl();
        addTransitionPostFunctionPage.submit();
        assertEquals(url, product.getTester().getDriver().getCurrentUrl());
    }

    private static void ensureDefaultWorkflowActivated()
    {
        String projectKey = RandomStringUtils.randomAlphabetic(6).toUpperCase();
        product.backdoor().project().addProject(projectKey, projectKey,
                testUserFactory.basicUser().getUsername());
    }

    private ViewWorkflowTransitionPage goToFirstTransition(String workflowName, ViewWorkflowSteps workflowStepsPage)
    {
        ViewWorkflowSteps.WorkflowStepItem firstStep = workflowStepsPage.getWorkflowStepItems().iterator().next();
        ViewWorkflowSteps.Transition firstTransition = firstStep.getTransitions().iterator().next();
        return workflowStepsPage.goToEditTransition(firstTransition.getTransition(), JiraWorkflow.LIVE, workflowName,
                firstStep.getStepNumber(), firstTransition.getTransitionNumber());
    }
}
