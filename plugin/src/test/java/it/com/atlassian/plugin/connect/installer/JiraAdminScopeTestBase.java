package it.com.atlassian.plugin.connect.installer;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JiraAdminScopeTestBase extends AdminScopeTestBase
{
    private final GlobalPermissionManager jiraPermissionManager;

    public JiraAdminScopeTestBase(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder, GlobalPermissionManager jiraPermissionManager)
    {
        super(testPluginInstaller, jwtApplinkFinder);
        this.jiraPermissionManager = checkNotNull(jiraPermissionManager);
    }

    @Override
    protected boolean isAdmin(String username)
    {
        return jiraPermissionManager.hasPermission(Permissions.Permission.ADMINISTER.getId(), ApplicationUsers.byKey(username));
    }
}
