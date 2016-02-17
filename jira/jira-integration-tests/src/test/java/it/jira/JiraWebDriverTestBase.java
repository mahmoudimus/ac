package it.jira;

import java.rmi.RemoteException;

import com.atlassian.connect.test.jira.pageobjects.workflow.ExtendedViewWorkflowTransitionPage;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import com.atlassian.plugin.connect.test.jira.util.JiraTestUserFactory;
import com.atlassian.testutils.annotations.Retry;
import com.atlassian.testutils.junit.RetryRule;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import it.jira.project.TestProject;
import it.jira.util.JiraTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

@Retry (maxAttempts = JiraWebDriverTestBase.MAX_RETRY_ATTEMPTS)
public class JiraWebDriverTestBase
{

    protected static JiraTestedProduct product = new JiraTestedProductAccessor().getJiraProduct();

    protected static TestProject project;

    protected static ConnectTestUserFactory testUserFactory;

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    @Rule
    public RetryRule retryRule = new RetryRule();
    public static final int MAX_RETRY_ATTEMPTS = 3;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        testUserFactory = new JiraTestUserFactory(product);
        product.getPageBinder().override(ViewWorkflowTransitionPage.class, ExtendedViewWorkflowTransitionPage.class);
        project = JiraTestHelper.addProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        product.backdoor().project().deleteProject(project.getKey());
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected static void login(TestUser user)
    {
        logout();
        product.quickLogin(user.getUsername(), user.getPassword());
    }

    protected static <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();
        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }
}
