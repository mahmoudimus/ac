package it.util;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.helptips.JiraHelpTipApiClient;

public class JiraTestUserFactory extends ConnectTestUserFactory
{

    private final JiraTestedProduct product;
    private final Backdoor backdoor;

    public JiraTestUserFactory(JiraTestedProduct product)
    {
        this(product, new Backdoor(new TestKitLocalEnvironmentData()));
    }

    public JiraTestUserFactory(JiraTestedProduct product, Backdoor backdoor)
    {
        this.product = product;
        this.backdoor = backdoor;
    }

    @Override
    protected TestUser createTestUser(AuthLevel authLevel, String username)
    {
        TestUser testUser = new TestUser(username);
        backdoor.usersAndGroups().addUser(username);
        addJiraPermissionsForTestUser(testUser, authLevel);
        return testUser;
    }

    @Override
    protected HelpTipApiClient getHelpTipApiClient(TestUser testUser)
    {
        return new JiraHelpTipApiClient(product, testUser);
    }

    private void addJiraPermissionsForTestUser(TestUser testUser, AuthLevel authLevel)
    {
        switch (authLevel)
        {
            case ADMIN:
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-administrators");
            case BASIC_USER:
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-users");
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-developers");
        }
    }
}
