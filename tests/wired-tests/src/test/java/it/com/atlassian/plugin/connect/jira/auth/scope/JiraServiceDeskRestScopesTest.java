package it.com.atlassian.plugin.connect.jira.auth.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeManagerTest;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;

import static it.com.atlassian.plugin.connect.jira.auth.scope.JiraScopeTestHelper.emptyBodyForJira;
import static java.util.Arrays.asList;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraServiceDeskRestScopesTest extends ScopeManagerTest
{
    public JiraServiceDeskRestScopesTest(AddonScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }

    public static Collection<ScopeTestData> testData()
    {
        List<ScopeTestData> params = new ArrayList<>();

        final String publicServiceDeskApiPath = "jira/rest/servicedeskapi/";

        params.addAll(asList(
                // info
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "info", true),

                // request
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicServiceDeskApiPath + "request", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123", true),

                // comment
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicServiceDeskApiPath + "request/10000/comment", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicServiceDeskApiPath + "request/SD-123/comment", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/comment", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/comment", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/comment/1234", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/comment/1234", true),

                // participant
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicServiceDeskApiPath + "request/10000/participant", true),
                emptyBodyForJira(ScopeName.WRITE, HttpMethod.POST, publicServiceDeskApiPath + "request/SD-123/participant", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, publicServiceDeskApiPath + "request/10000/participant", true),
                emptyBodyForJira(ScopeName.DELETE, HttpMethod.DELETE, publicServiceDeskApiPath + "request/SD-123/participant", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/participant", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/participant", true),

                // sla
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/sla", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/sla", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/sla/1234", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/sla/1234", true),

                // status
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/10000/status", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "request/SD-123/status", true),

                // servicedesk
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000", true),

                // queue
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/queue", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/queue/1234", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/queue/1234/issue", true),

                // requesttype
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/requesttype", true),
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/requesttype/1234", true),

                // field
                emptyBodyForJira(ScopeName.READ, HttpMethod.GET, publicServiceDeskApiPath + "servicedesk/10000/requesttype/1234/field", true)

                ));
        return params;
    }
}
