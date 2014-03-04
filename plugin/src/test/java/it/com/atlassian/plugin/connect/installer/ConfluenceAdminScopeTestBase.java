package it.com.atlassian.plugin.connect.installer;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConfluenceAdminScopeTestBase extends AdminScopeTestBase
{
    private final PermissionManager confluencePermissionManager;
    private final UserManager userManager;

    public ConfluenceAdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                                        JwtApplinkFinder jwtApplinkFinder,
                                        PermissionManager confluencePermissionManager,
                                        UserManager userManager)
    {
        super(testPluginInstaller, jwtApplinkFinder);
        this.confluencePermissionManager = checkNotNull(confluencePermissionManager);
        this.userManager = checkNotNull(userManager);
    }

    @Override
    protected boolean isAdmin(String username) throws EntityException
    {
        return confluencePermissionManager.isConfluenceAdministrator(userManager.getUser(username));
    }
}
