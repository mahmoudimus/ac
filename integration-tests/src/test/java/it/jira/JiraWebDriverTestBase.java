package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.AddPermissionPage;
import com.atlassian.jira.pageobjects.pages.EditPermissionsPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.workflow.ExtendedViewWorkflowTransitionPage;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import hudson.plugins.jira.soap.RemoteProject;
import it.util.ConnectTestUserFactory;
import it.util.JiraTestUserFactory;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

public class JiraWebDriverTestBase
{

    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();

    /**
     * @deprecated use {@code product.backdoor()} instead
     */
    @Deprecated()
    protected static JiraOps jiraOps;

    protected static RemoteProject project;

    protected static ConnectTestUserFactory testUserFactory;

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    private static final int DEFAULT_PERMISSION_SCHEMA = 0;
    private static final int JIRA_PERMISSION_BROWSE_PROJECTS = 10;
    private static final String JIRA_GROUP_ANYONE = "";

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        TestBase.funcTestHelper.administration.switchToLicense(new LicenseKeys.License("AAAEyg0ODAoPeNqtWFtvq0YQfvevsNSX9oEVvpwm50iWmjiumipOoiTta7XAAFvDLmd3sY/z67tgn\n" +
                "OBlwJDmwZKNh5lvZr657P70EufjPykfu9Px5OLbl6/fvszHy+eX8dSdzEdLwTX19T1NYbGmuRTj3\n" +
                "0FyygN4HV/TgLGRL1JCdUKVYpQTGgSCE19wDr4m/zJJaa7Fjmo/JtcsSRiPHkEyESzc8k3zoQnVY\n" +
                "P72Kd9S1ZASMiJqx0JdaA2THLgPRFMvAWKAsS0stMyhVKYyKjW8lmZJluQR44p4Qm6IDtW8fFp/5\n" +
                "T5PPZAP4V8KpFo4k1Epccd84ApWAdNM8MXq/mX19Ph0+7waeTT1hCBXxqEn4LBbhDRRhWUeNlAXq\n" +
                "pwwoVmmnGM0MP89pgmNmHHGfHOUFnKf0mxIEDCtUcLCcE8Y1xBJWvhxiAkm2y93PE//EEpDcAjW1\n" +
                "HXdg+PNaKTEqKH70yzcFI9uDWqumUFmZ+78G5idDRe7BIIIUuMXUcADLfY0TUE2PK2y+rLPoOTy8\n" +
                "mG9Xj0tb6/uDm7UAZUwMIcrAhx5c13+fIJUaLiKDE61mLiHoObmpa0wNt/cKQlxiLDtChOExvQt8\n" +
                "HUoa1rk0NSbD6sfGZP7G1MrBszlzHEnjjsdeQY8k9DQifEPzyFWgw2EigjJInHIkIY0E87BsRO0u\n" +
                "1ioRIiNCfxZoxumRSIi5pOM6jhkpqPIBi06w2inGM3OY0J5WdurLU3yshTwqGM4LRE7Ks+5p3zJs\n" +
                "lJpiTmTgngQ0y2QuFR24tFbpR2D40Sm1lXDa6Cve4+Rhnj1h1MmAauH91Iu4mbCFoDaNOIUSQAei\n" +
                "yyzAl7FzxbvyL0t+phLP6YKKpJO5iVJZyMNNP3Hp4kpUCpVKyUY34SmE2bM3xholY1av6v9ianAK\n" +
                "F/3TxiKpWZyVb/aOyoW2maN9MksBjOmcgtKo1Y6+d5oUY0CqHqBzad3WtRcPRp4f+R8zw0uw2WFz\n" +
                "okB2akDOLprq7Q4YWNum2B1OUtF93CrVwQW+l5FV7f+ICPKmTq0lLRYjRyVZ5mQ+oNBP6nFKpUYg\n" +
                "er1276sHGvxZGxUFTntWj2w5GEweELolimG+cfLHgxBDGZF5NF7PdqRTw74/zZaC/zTrrLEQLQVH\n" +
                "saFjm2hB/tqcGzpDsWfFjjb5vfcdDJRUPKNZ5jPnQ1l0GjGhJvjbsCK26/LtmH08sgwhphYpSU+P\n" +
                "OVNqR5xRDdNLCTYzvXBfbjNzW4i2rbqnQHdX6uu8j/bO/Z6j6PXwJocsIN27OrtDRJbVjGIvQZDW\n" +
                "/6GTwGMP00ad29/fW21oe7lcssJqzZvcp6wlBn1SKOwHUCX9zthpnt1sjIrfGshDlhZ0fDiDerTe\n" +
                "jfGqvMt4czChDnSowjt16r7nZU5aCaLNDze7XjF1c5vdUalAy4LMGxtC92Zw3CfexXMHNLX+4+Sf\n" +
                "rMOk1pKKH2zT0CN7XFgy7VdvIH3k+fV0cPxA78pTzjjn5ciTXPO9P6XjjW4jqc+OmxjbUuWLYd11\n" +
                "GGb1fk2WsePSkuxCwZabblI6Hskab0aa/MG4Sa2dNef3VGl1yJgIYNgMZldXl64F5Ov89n8cvS8u\n" +
                "l+YjzNzJ+7819mAMj13ddO1XnxwB/i0noop+g9E03YLMCwCFCKZCUT2Yo0NWE81lHRLtxBroijAA\n" +
                "hQsO+wfbOOFJztaLbaOljh7nUOCkQ==X021nu", "Atlassian OnDemand (Community)", "mauro-support", "SEN-3010463", 999999999));

        testUserFactory = new JiraTestUserFactory(product);
        
        product.getPageBinder().override(ViewWorkflowTransitionPage.class, ExtendedViewWorkflowTransitionPage.class);

        jiraOps = new JiraOps(product.getProductInstance());
        project = jiraOps.createProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    protected void testLoggedInAndAnonymous(final Callable runnable) throws Exception
    {
        login(testUserFactory.basicUser());
        runnable.call();
        logout();
        runWithAnonymousUsePermission(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                runnable.call();
                return null;
            }
        });
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void login(TestUser user)
    {
        logout();
        product.quickLogin(user.getUsername(), user.getPassword());
    }

    protected <T> T loginAndRun(TestUser user, Callable<T> test) throws Exception
    {
        logout();
        login(user);
        try
        {
            return test.call();
        } finally
        {
            logout();
        }
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();
        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }

    public static <T> T runWithAnonymousUsePermission(Callable<T> test) throws Exception
    {
        final AddPermissionPage addPermissionPage = product.quickLoginAsAdmin(AddPermissionPage.class,
                DEFAULT_PERMISSION_SCHEMA, JIRA_PERMISSION_BROWSE_PROJECTS);
        addPermissionPage.setGroup(JIRA_GROUP_ANYONE);
        addPermissionPage.add();
        try
        {
            return test.call();
        } finally
        {
            EditPermissionsPage editPermissionsPage = product.quickLoginAsAdmin(EditPermissionsPage.class, DEFAULT_PERMISSION_SCHEMA);
            editPermissionsPage.deleteForGroup("Browse Projects", JIRA_GROUP_ANYONE);
        }

    }
}
