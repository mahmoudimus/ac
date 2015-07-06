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

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.rpcJsonBodyForJira;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraJsonRpcScopesTest extends ScopeManagerTest
{
    private static final String PATH = "/jira/rpc/json-rpc/jirasoapservice-v2";

    public JiraJsonRpcScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
            super(scopeManager, scopeTestHelper, testData());
    }

    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                // reads
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssue", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromJqlSearch", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getVersions", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithProject", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getPriorities", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatars", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComponents", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComment", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectById", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getAttachmentsFromIssue", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getGroup", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatar", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueById", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getUser", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSecurityLevel", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComments", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueTypesForProject", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSubTaskIssueTypesForProject", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateByKey", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectsNoSchemes", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForAction", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getStatuses", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateById", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectByKey", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithLimit", true),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutions", true),

                // writes
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForCreate", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForEdit", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getSecurityLevels", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParent", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "progressWorkflowAction", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "updateIssue", true),
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssue", true),

                // implied scopes
                rpcJsonBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getIssue", true),
                rpcJsonBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcJsonBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcJsonBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "updateIssue", true),
                rpcJsonBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "updateIssue", true),

                // one thing wrong
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "does not exist", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParent", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "progressWorkflowAction", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "updateIssue", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssue", false),
                rpcJsonBodyForJira(null, HttpMethod.POST, PATH, "getIssue", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.PUT, PATH, "getIssue", false),
                rpcJsonBodyForJira(ScopeName.READ, HttpMethod.DELETE, PATH, "getIssue", false));
    }
}
