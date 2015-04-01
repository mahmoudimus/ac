package it.util;

import com.atlassian.confluence.it.ConfluenceBaseUrlSelector;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.usermanagement.DefaultDirectoryConfiguration;
import com.atlassian.confluence.it.usermanagement.DefaultUserManagementHelper;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.TestedProduct;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectTestUserFactory
{
    private static final DefaultUserManagementHelper userManager;
    
    private static final AtomicInteger userNameCounter = new AtomicInteger();

    static
    {
        ConfluenceBaseUrlSelector confluenceBaseUrlSelector = new ConfluenceBaseUrlSelector();
        ConfluenceRpc confluenceRpc = ConfluenceRpc.newInstance(confluenceBaseUrlSelector.getBaseUrl());
        DefaultDirectoryConfiguration defaultDirectoryConfiguration = new DefaultDirectoryConfiguration();
        userManager = new DefaultUserManagementHelper(confluenceRpc, defaultDirectoryConfiguration);
    }

    public static TestUser sysadmin(TestedProduct product)
    {
        String username = "sysadmin-" + incrementCounter();
        
        if (product instanceof JiraTestedProduct)
        {
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUser(username);
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-users");
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-administrators");
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-developers");
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-sysadmin");
        }
        else
        {
            userManager.createUser(new User(username, username, username, username + "@example.com"));
            userManager.addUserToGroup(username, "confluence-users");
            userManager.addUserToGroup(username, "confluence-administrators");
            userManager.addUserToGroup(username, "confluence-developers");      // ??????
            userManager.addUserToGroup(username, "confluence-sysadmin");        // ??????
        }
        
        return new TestUser(username);
    }

    public static TestUser admin(TestedProduct product)
    {
        String username = "admin-" + incrementCounter();
        
        if (product instanceof JiraTestedProduct)
        {
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUser(username);
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-users");
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-administrators");
        }
        else
        {
            userManager.createUser(new User(username, username, username, username + "@example.com"));
            userManager.addUserToGroup(username, "confluence-users");
            userManager.addUserToGroup(username, "confluence-administrators");
        }
        return new TestUser(username);
    }

    public static TestUser basicUser(TestedProduct product)
    {
        String username = "user-" + incrementCounter();
        if (product instanceof JiraTestedProduct)
        {
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUser(username);
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(username, "jira-users");
        }
        else 
        {
            userManager.createUser(new User(username, username, username, username + "@example.com"));
            userManager.addUserToGroup(username, "confluence-users");
        }
        return new TestUser(username);
    }
    
    private static String incrementCounter()
    {
        return String.valueOf(userNameCounter.incrementAndGet());
    }
}
