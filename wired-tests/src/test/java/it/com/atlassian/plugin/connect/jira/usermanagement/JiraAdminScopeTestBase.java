package it.com.atlassian.plugin.connect.jira.usermanagement;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.sal.api.user.UserKey;
import it.com.atlassian.plugin.connect.plugin.usermanagement.AdminScopeTestBase;

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
    protected boolean isUserTopLevelAdmin(UserKey userKey)
    {
        return jiraPermissionManager.hasPermission(Permissions.Permission.ADMINISTER.getId(), getUser(userKey));
    }

    private ApplicationUser getUser(UserKey userKey)
    {
        return userManager.getUserByKey(userKey.getStringValue());
    }
}
