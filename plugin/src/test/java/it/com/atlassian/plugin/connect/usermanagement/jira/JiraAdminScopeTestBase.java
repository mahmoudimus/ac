package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import it.com.atlassian.plugin.connect.usermanagement.AdminScopeTestBase;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JiraAdminScopeTestBase extends AdminScopeTestBase
{
    protected final PermissionManager jiraPermissionManager;
    protected final UserManager userManager;

    public JiraAdminScopeTestBase(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder,
            PermissionManager jiraPermissionManager, UserManager userManager)
    {
        super(testPluginInstaller, jwtApplinkFinder);
        this.userManager = userManager;
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
    }

    @Override
    protected boolean isAdmin(String username)
    {
        return jiraPermissionManager.hasPermission(Permissions.Permission.ADMINISTER.getId(), ApplicationUsers.byKey(username));
    }

    protected ApplicationUser getAddonUser()
    {
        return userManager.getUserByKey(getAddonUserKey());
    }
}
