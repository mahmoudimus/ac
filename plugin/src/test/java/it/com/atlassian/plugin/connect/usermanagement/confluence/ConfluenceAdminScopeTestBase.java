package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;
import it.com.atlassian.plugin.connect.usermanagement.AdminScopeTestBase;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConfluenceAdminScopeTestBase extends AdminScopeTestBase
{
    protected final PermissionManager confluencePermissionManager;
    protected final UserManager userManager;

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

    protected ConfluenceUser getAddonUser()
    {
        return FindUserHelper.getUserByUsername(getAddonUserKey());
    }
}
