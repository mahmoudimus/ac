package it.com.atlassian.plugin.connect.usermanagement.confluence;

import com.atlassian.confluence.cache.ThreadLocalCache;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.UserManager;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.runner.RunWith;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceAdminScopeTest extends ConfluenceAdminScopeTestBase
{
    public ConfluenceAdminScopeTest(TestPluginInstaller testPluginInstaller,
                                    JwtApplinkFinder jwtApplinkFinder,
                                    PermissionManager confluencePermissionManager,
                                    UserManager userManager,
                                    TestAuthenticator testAuthenticator)
    {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, userManager, testAuthenticator);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.ADMIN;
    }

    @Override
    protected ScopeName getScopeOneDown()
    {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return true;
    }

    @Override
    protected boolean isUserAdmin(String username)
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
