package it.util;



import com.atlassian.confluence.test.ConfluenceBaseUrlSelector;
import com.atlassian.confluence.test.api.model.person.UserWithDetails;
import com.atlassian.confluence.test.rpc.RpcBaseResolver;
import com.atlassian.confluence.test.rpc.VersionedRpcBaseResolver;
import com.atlassian.confluence.test.rpc.api.ConfluenceRpcClient;
import com.atlassian.confluence.test.usermanagement.DefaultDirectoryConfiguration;
import com.atlassian.confluence.test.usermanagement.TestUserFactory;
import com.atlassian.confluence.test.usermanagement.DefaultUserManager;


import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.pageobjects.TestedProduct;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectTestUserFactory
{
    private static final TestUserFactory testUserFactory;
    private static final DefaultUserManager userManager;
    
    private static final AtomicInteger userNameCounter = new AtomicInteger();

    static
    {
        ConfluenceBaseUrlSelector confluenceBaseUrlSelector = new ConfluenceBaseUrlSelector();
        RpcBaseResolver versionedRpcBaseResolver = VersionedRpcBaseResolver.V1;
        ConfluenceRpcClient confluenceRpcClient = new ConfluenceRpcClient(confluenceBaseUrlSelector, versionedRpcBaseResolver);
        DefaultDirectoryConfiguration defaultDirectoryConfiguration = new DefaultDirectoryConfiguration(confluenceRpcClient);
        userManager = new DefaultUserManager(confluenceRpcClient, defaultDirectoryConfiguration);
        testUserFactory = new TestUserFactory(userManager);
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
            userManager.createUser(new UserWithDetails(null, username, username, username, username + "@example.com"));
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
            userManager.createUser(new UserWithDetails(null, username, username, username, username + "@example.com"));
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
            userManager.createUser(new UserWithDetails(null, username, username, username, username + "@example.com"));
            userManager.addUserToGroup(username, "confluence-users");
        }
        return new TestUser(username);
    }
    
    private static String incrementCounter()
    {
        return String.valueOf(userNameCounter.incrementAndGet());
    }
}
