package it.capabilities.jira;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraWorkflowTransitionPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAddWorkflowTransitionFunctionParamsPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.CheckUsernameConditionServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

public class TestWorkflowPostFunction extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private static final String WORKFLOW_POST_FUNCTION_PAGE = "ac-workflow-post-function";
    private static final String WORKFLOW_NAME = "classic default workflow";
    private static final Integer WORKFLOW_STEP = 1;
    private static final Integer WORKFLOW_TRANSITION = 5;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.helloWorldServlet())
                .addModule("jiraWorkflowPostFunctions",
                        newWorkflowPostFunctionBean()
                                .withName(new I18nProperty("My function", null))
                                .withKey(WORKFLOW_POST_FUNCTION_PAGE)
                                .withView(new UrlBean("/wpf-view"))
                                .withEdit(new UrlBean("/wpf-edit"))
                                .withCreate(new UrlBean("/wpf-create"))
                                .withTriggered(new UrlBean("/wpf-triggered"))
                                .withDescription(new I18nProperty("workflow post function description", null))
                                .build())
                .addRoute("/wpf-view", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-edit", ConnectAppServlets.helloWorldServlet())
                .addRoute("/wpf-create", ConnectAppServlets.workflowPostFunctionServlet())
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
        JiraWorkflowTransitionPage workflowTransitionPage = product.visit(JiraWorkflowTransitionPage.class, "draft", WORKFLOW_NAME, WORKFLOW_STEP, WORKFLOW_TRANSITION);
        JiraAddWorkflowTransitionFunctionParamsPage addonPage = workflowTransitionPage.addPostFunction("my-plugin", WORKFLOW_POST_FUNCTION_PAGE);
        assertThat(addonPage.isLoaded(), equalTo(true));
        addonPage.submitWorkflowParams();
        String workflowConfiguration = workflowTransitionPage.workflowPostFunctionConfigurationValue(WORKFLOW_POST_FUNCTION_PAGE);
        assertEquals("workflow configuration text", workflowConfiguration);
    }



}
