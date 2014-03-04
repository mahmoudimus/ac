package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraAdminScopeTest extends JiraAdminScopeTestBase
{
    public JiraAdminScopeTest(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder,
            PermissionManager jiraPermissionManager, UserManager userManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, jiraPermissionManager, userManager);
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
