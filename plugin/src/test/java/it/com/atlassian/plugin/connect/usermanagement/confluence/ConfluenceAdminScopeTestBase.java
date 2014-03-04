package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.UserManager;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.usermanagement.AdminScopeTestBase;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConfluenceAdminScopeTestBase extends AdminScopeTestBase
{
    protected final PermissionManager confluencePermissionManager;
    protected final UserManager userManager;

    public ConfluenceAdminScopeTestBase(TestPluginInstaller testPluginInstaller,
                                        JwtApplinkFinder jwtApplinkFinder,
                                        PermissionManager confluencePermissionManager,
                                        UserManager userManager,
                                        TestAuthenticator testAuthenticator)
    {
        super(testPluginInstaller, jwtApplinkFinder, testAuthenticator);
        this.confluencePermissionManager = checkNotNull(confluencePermissionManager);
        this.userManager = checkNotNull(userManager);
    }

    @Override
    protected boolean isUserKeyAdmin(String userKey)
    {
        return confluencePermissionManager.isConfluenceAdministrator(getUserByKey(userKey));
    }

    protected ConfluenceUser getAddonUser()
    {
        return getUserByKey(getAddonUserKey());
    }

    private ConfluenceUser getUserByKey(String userKey)
    {
        UserKey key = new UserKey(userKey);
        return FindUserHelper.getUserByUserKey(key);
    }
}
