package it.com.atlassian.plugin.connect.usermanagement.jira;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jwt.applinks.JwtApplinkFinder;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;

public class JiraAdminScopeTest extends JiraAdminScopeTestBase
{
    public JiraAdminScopeTest(TestPluginInstaller testPluginInstaller, JwtApplinkFinder jwtApplinkFinder, GlobalPermissionManager jiraPermissionManager)
    {
        super(testPluginInstaller, jwtApplinkFinder, jiraPermissionManager);
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
