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

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.rpcSoapBodyForJira;


@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraSoapRpcScopesTest extends ScopeManagerTest
{
    private static final String PATH = "/jira/rpc/soap/jirasoapservice-v2";

    public JiraSoapRpcScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
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
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssue", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromJqlSearch", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getVersions", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithProject", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getPriorities", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatars", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComponents", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComment", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectById", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getAttachmentsFromIssue", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getGroup", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatar", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueById", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getUser", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSecurityLevel", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComments", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueTypesForProject", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSubTaskIssueTypesForProject", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateByKey", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectsNoSchemes", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForAction", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getStatuses", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateById", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectByKey", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithLimit", true),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutions", true),

                // writes
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForCreate", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForEdit", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getSecurityLevels", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParent", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "progressWorkflowAction", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "updateIssue", true),
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssue", true),

                // implied scopes
                rpcSoapBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getIssue", true),
                rpcSoapBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcSoapBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcSoapBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "updateIssue", true),
                rpcSoapBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "updateIssue", true),

                // one thing wrong
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "does_not_exist", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParent", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "progressWorkflowAction", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "updateIssue", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssue", false),
                rpcSoapBodyForJira(null, HttpMethod.POST, PATH, "getIssue", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.PUT, PATH, "getIssue", false),
                rpcSoapBodyForJira(ScopeName.READ, HttpMethod.DELETE, PATH, "getIssue", false));
    }
}
