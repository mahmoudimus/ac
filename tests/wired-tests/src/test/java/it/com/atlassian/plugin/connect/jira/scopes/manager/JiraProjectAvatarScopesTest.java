package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.emptyBodyForJira;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraProjectAvatarScopesTest extends ScopeManagerTest
{
    private static final String PROJECT_AVATAR_URL = "/jira/secure/projectavatar";

    public JiraProjectAvatarScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }
    
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                // happy path
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, PROJECT_AVATAR_URL, true),

                // higher scopes
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.GET, PROJECT_AVATAR_URL, true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.GET, PROJECT_AVATAR_URL, true),
                emptyBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.GET, PROJECT_AVATAR_URL, true),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.GET, PROJECT_AVATAR_URL, true),

                // bad paths
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/projectavatarpagan", false),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, "/jira/secure/projectavatar/lordbritish", false),

                // bad method type
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PROJECT_AVATAR_URL, false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.PUT, PROJECT_AVATAR_URL, false),
                emptyBodyForJira(ScopeName.ADMIN, HttpMethod.DELETE, PROJECT_AVATAR_URL, false),

                // no scope
                emptyBodyForJira(null, HttpMethod.GET, PROJECT_AVATAR_URL, false));
    }
}
