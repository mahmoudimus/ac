package com.atlassian.plugin.connect.test.jira.util;

import java.util.List;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.connect.test.common.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;

public class JiraTestUserFactory extends ConnectTestUserFactory {

    private static final String ONDEMAND_ADMINISTRATORS_GROUP = "administrators";
    private static final String DEFAULT_ADMINISTRATORS_GROUP = "jira-administrators";

    private final JiraTestedProduct product;
    private final Backdoor backdoor;

    public JiraTestUserFactory(JiraTestedProduct product) {
        this.product = product;
        this.backdoor = product.backdoor();
    }

    @Override
    protected TestUser createTestUser(AuthLevel authLevel, String username) {
        TestUser testUser = new TestUser(username);
        backdoor.usersAndGroups().addUser(username);
        addJiraPermissionsForTestUser(testUser, authLevel);
        return testUser;
    }

    @Override
    protected HelpTipApiClient getHelpTipApiClient(TestUser testUser) {
        return new JiraHelpTipApiClient(product, testUser);
    }

    private void addJiraPermissionsForTestUser(TestUser testUser, AuthLevel authLevel) {
        List<String> adminGroups = backdoor.permissions().getGlobalPermissionGroups(Permissions.ADMINISTER);
        String adminGroup = (adminGroups.contains(DEFAULT_ADMINISTRATORS_GROUP)) ? DEFAULT_ADMINISTRATORS_GROUP : ONDEMAND_ADMINISTRATORS_GROUP;

        switch (authLevel) {
            case ADMIN:
                backdoor.usersAndGroups().addUserToGroup(testUser.getUsername(), adminGroup);
        }
    }
}
