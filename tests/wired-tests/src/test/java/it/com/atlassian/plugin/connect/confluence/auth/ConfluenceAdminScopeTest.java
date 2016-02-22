package it.com.atlassian.plugin.connect.confluence.auth;

import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.runner.RunWith;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceAdminScopeTest extends ConfluenceAdminScopeTestBase {
    public ConfluenceAdminScopeTest(TestPluginInstaller testPluginInstaller,
                                    JwtApplinkFinder jwtApplinkFinder,
                                    PermissionManager confluencePermissionManager,
                                    TestAuthenticator testAuthenticator) {
        super(testPluginInstaller, jwtApplinkFinder, confluencePermissionManager, testAuthenticator);
    }

    @Override
    protected ScopeName getScope() {
        return ScopeName.ADMIN;
    }

    @Override
    protected ScopeName getScopeOneDown() {
        return ScopeName.SPACE_ADMIN;
    }

    @Override
    protected boolean shouldBeTopLevelAdmin() {
        return true;
    }
}
