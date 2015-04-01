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
    private static final DefaultUserManager userManager;
    
    private static final AtomicInteger userNameCounter = new AtomicInteger();

    static
    {
        ConfluenceBaseUrlSelector confluenceBaseUrlSelector = new ConfluenceBaseUrlSelector();
        RpcBaseResolver versionedRpcBaseResolver = VersionedRpcBaseResolver.V1;
        ConfluenceRpcClient confluenceRpcClient = new ConfluenceRpcClient(confluenceBaseUrlSelector, versionedRpcBaseResolver);
        DefaultDirectoryConfiguration defaultDirectoryConfiguration = new DefaultDirectoryConfiguration(confluenceRpcClient);
        userManager = new DefaultUserManager(confluenceRpcClient, defaultDirectoryConfiguration);
    }
    
    public static TestUser sysadmin(TestedProduct product)
    {
        return createTestUser(product, AuthLevel.SYSADMIN);
    }

    public static TestUser admin(TestedProduct product)
    {
        return createTestUser(product, AuthLevel.ADMIN);
    }

    public static TestUser basicUser(TestedProduct product)
    {
        return createTestUser(product, AuthLevel.BASIC_USER);
    }
    
    private static String incrementCounter()
    {
        return String.valueOf(userNameCounter.incrementAndGet());
    }

    private static TestUser createTestUser(TestedProduct product, AuthLevel authLevel)
    {
        String username = authLevel.getPrefix() + "-" + incrementCounter();
        TestUser testUser = new TestUser(username);
        if(product instanceof JiraTestedProduct)
        {
            TestBase.funcTestHelper.backdoor.usersAndGroups().addUser(username);
            addJiraPermissionsForTestUser(testUser, authLevel);
        }
        else
        {
            userManager.createUser(new UserWithDetails(null, username, username, username, username + "@example.com"));
            addConfluencePermissionsForTestUser(testUser, authLevel);
        }
        return testUser;
    }

    private static void addJiraPermissionsForTestUser(TestUser testUser, AuthLevel authLevel)
    {
        switch (authLevel)
        {
            case SYSADMIN:
                TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-sysadmin");
                TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-developers");
            case ADMIN:
                TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-administrators");
            case BASIC_USER:
                TestBase.funcTestHelper.backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-users");
        }
    }

    private static void addConfluencePermissionsForTestUser(TestUser testUser, AuthLevel authLevel)
    {
        switch (authLevel)
        {
            case SYSADMIN:
                userManager.addUserToGroup(testUser.getUsername(), "confluence-developers");      // ??????
                userManager.addUserToGroup(testUser.getUsername(), "confluence-sysadmin");        // ??????
            case ADMIN:
                userManager.addUserToGroup(testUser.getUsername(), "confluence-administrators");
            case BASIC_USER:
                userManager.addUserToGroup(testUser.getUsername(), "confluence-users");
        }
    }

    enum AuthLevel
    {
        SYSADMIN("sysadmin"), ADMIN("admin"), BASIC_USER("user");

        private String prefix;

        AuthLevel(String prefix)
        {
            this.prefix = prefix;
        }

        public String getPrefix()
        {
            return prefix;
        }

    }

}

