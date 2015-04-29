package it.com.atlassian.plugin.connect.confluence.usermanagement;

import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.user.UserManager;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.plugin.usermanagement.AdminScopeTestBase;

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

    protected ConfluenceUser getUser(String username)
    {
        return FindUserHelper.getUserByUsername(username);
    }

    @Override
    protected boolean isUserTopLevelAdmin(String username)
    {
        // now flush the permissions cache so that it rebuilds to reflect new permission sets
        //
        // this is needed because Confluence's CachingSpacePermissionManager caches permissions in ThreadLocalCache
        // and doesn't realise when the permissions have changed
        //
        // the alternative is to flush the cache in the prod code, which may have unintended side-effects
        ThreadLocalCache.flush();

        return confluencePermissionManager.isConfluenceAdministrator(getUser(username));
    }
}
