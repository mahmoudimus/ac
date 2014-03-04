package it.com.atlassian.plugin.connect.installer;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.UserManager;
import org.junit.runner.RunWith;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceAdminScopeTest extends ConfluenceAdminScopeTestBase
{
    public ConfluenceAdminScopeTest(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder, PermissionManager confluencePermissionManager, UserManager userManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, userManager);
    }

    @Override
    protected ScopeName getScope()
    {
        return ScopeName.ADMIN;
    }

    @Override
    protected boolean shouldBeAdmin()
    {
        return true;
    }
}
