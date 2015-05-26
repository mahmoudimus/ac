package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static it.com.atlassian.plugin.connect.jira.util.JiraScopeTestHelper.rpcBodyForJira;


@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class JiraSoapRpcScopesTest extends RequestInApiScopeTest
{
    private static final String PATH = "/jira/rpc/soap/jirasoapservice-v2";

    public JiraSoapRpcScopesTest(AddOnScopeManager scopeManager, TestPluginInstaller testPluginInstaller)
    {
        super(scopeManager, testPluginInstaller, testData());
    }

    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                // reads
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssue", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromJqlSearch", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getVersions", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithProject", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getPriorities", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatars", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComponents", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComment", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectById", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getAttachmentsFromIssue", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getGroup", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatar", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueById", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getUser", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSecurityLevel", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getComments", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssueTypesForProject", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getSubTaskIssueTypesForProject", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateByKey", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectsNoSchemes", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForAction", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getStatuses", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateById", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getProjectByKey", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithLimit", true),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "getResolutions", true),

                // writes
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForCreate", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getFieldsForEdit", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getSecurityLevels", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParent", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "progressWorkflowAction", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "updateIssue", true),
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "createIssue", true),

                // implied scopes
                rpcBodyForJira(ScopeName.WRITE, HttpMethod.POST, PATH, "getIssue", true),
                rpcBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "getIssue", true),
                rpcBodyForJira(ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "updateIssue", true),
                rpcBodyForJira(ScopeName.ADMIN, HttpMethod.POST, PATH, "updateIssue", true),

                // one thing wrong
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "does_not_exist", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParent", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "progressWorkflowAction", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "updateIssue", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.POST, PATH, "createIssue", false),
                rpcBodyForJira(null, HttpMethod.POST, PATH, "getIssue", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.PUT, PATH, "getIssue", false),
                rpcBodyForJira(ScopeName.READ, HttpMethod.DELETE, PATH, "getIssue", false));
    }
}
