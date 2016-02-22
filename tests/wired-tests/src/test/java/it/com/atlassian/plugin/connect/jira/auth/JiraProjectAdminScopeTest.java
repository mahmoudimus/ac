package it.com.atlassian.plugin.connect.jira.auth;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.jira.auth.JiraAdminScopeTestBase;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraProjectAdminScopeTest extends JiraAdminScopeTestBase {
    public JiraProjectAdminScopeTest(TestPluginInstaller testPluginInstaller,
                                     JwtApplinkFinder jwtApplinkFinder,
                                     PermissionManager jiraPermissionManager,
                                     UserManager userManager,
                                     TestAuthenticator testAuthenticator) {
        super(testPluginInstaller, jwtApplinkFinder, jiraPermissionManager, userManager, testAuthenticator);
    }

    @Override
    protected ScopeName getScope() {
        return ScopeName.PROJECT_ADMIN;
    }

    @Override
    protected ScopeName getScopeOneDown() {
        return ScopeName.DELETE;
    }

    @Override
    protected boolean shouldBeTopLevelAdmin() {
        return false;
    }
}
