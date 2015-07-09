package it.util;

import java.util.List;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.helptips.JiraHelpTipApiClient;

public class JiraTestUserFactory extends ConnectTestUserFactory
{

    private final JiraTestedProduct product;
    private final Backdoor backdoor;

    public JiraTestUserFactory(JiraTestedProduct product)
    {
        this.product = product;
        this.backdoor = product.backdoor();
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
        List<String> adminGroups = backdoor.permissions().getGlobalPermissionGroups(Permissions.ADMINISTER);
        String adminGroup = (adminGroups.contains("jira-administrators")) ? "jira-administrators" : "administrators";

        List<String> developerGroups = backdoor.permissions().getGlobalPermissionGroups(Permissions.USER_PICKER);
        String developerGroup = (developerGroups.contains("jira-developers")) ? "jira-developers" : "developers";

        switch (authLevel)
        {
            case ADMIN:
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), adminGroup);
            case BASIC_USER:
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), "jira-users");
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), developerGroup);
        }
    }
}
