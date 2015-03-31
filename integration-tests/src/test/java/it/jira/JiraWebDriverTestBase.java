package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import hudson.plugins.jira.soap.RemoteProject;
import it.ConnectWebDriverTestBase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

public class JiraWebDriverTestBase extends ConnectWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected static RemoteProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        jiraOps = new JiraOps(product.getProductInstance());
        project = jiraOps.createProject();

        //TestBase.funcTestHelper.backdoor.restoreBlankInstance();
        //TestBase.funcTestHelper.backdoor.project().addProject("A Project Name", "KEY", "admin");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().deleteUser("barney");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserEvenIfUserExists("admin");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserEvenIfUserExists("barney", "barney", "Barney", "barney@example.com");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().deleteUser("betty");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserEvenIfUserExists("betty", "betty", "Betty", "betty@example.com");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().deleteUser("fred");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserEvenIfUserExists("fred");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("barney", "jira-users");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("betty", "jira-users");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("betty", "jira-administrators");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("fred", "jira-users");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("admin", "jira-administrators");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("fred", "jira-administrators");
        TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("fred", "jira-developers");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("admin", "jira-users");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("admin", "jira-administrators");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("admin", "jira-sysadmin");
        //TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup("admin", "jira-developers");

    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    protected void testLoggedInAndAnonymous(Callable runnable) throws Exception
    {
        getProduct().quickLoginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

    protected static JiraTestedProduct getProduct()
    {
        return (JiraTestedProduct) product;
    }

}
