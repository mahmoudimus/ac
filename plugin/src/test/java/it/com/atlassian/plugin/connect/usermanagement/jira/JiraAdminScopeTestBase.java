package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.usermanagement.AdminScopeTestBase;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JiraAdminScopeTestBase extends AdminScopeTestBase
{
    protected final PermissionManager jiraPermissionManager;
    protected final UserManager userManager;

    public JiraAdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                                  JwtApplinkFinder jwtApplinkFinder,
                                  PermissionManager jiraPermissionManager,
                                  UserManager userManager,
                                  TestAuthenticator testAuthenticator)
    {
        super(testPluginInstaller, jwtApplinkFinder, testAuthenticator);
        this.userManager = checkNotNull(userManager);
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
    }

    @Override
    protected boolean isUserTopLevelAdmin(String username)
    {
        return jiraPermissionManager.hasPermission(Permissions.Permission.ADMINISTER.getId(), getUser(username));
    }

    private ApplicationUser getUser(String username)
    {
        return userManager.getUserByName(username);
    }
}