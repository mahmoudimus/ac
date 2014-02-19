package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class JiraSoapRpcScopesTest extends AbstractScopesTest
{
    private static final String PATH = "/jira/rpc/soap/jirasoapservice-v2";

    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} {3} --> {4}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]
        {
            // reads
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssue", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromJqlSearch", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getVersions", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithProject", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getSecurityLevels", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getPriorities", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatars", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getComponents", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getComment", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getProjectById", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getAttachmentsFromIssue", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getGroup", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getProjectAvatar", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForEdit", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssueById", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getUser", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getSecurityLevel", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getComments", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssueTypesForProject", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getSubTaskIssueTypesForProject", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateByKey", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getProjectsNoSchemes", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForAction", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getStatuses", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getFieldsForCreate", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getResolutionDateById", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getProjectByKey", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getIssuesFromTextSearchWithLimit", true},
            {ScopeName.READ, HttpMethod.POST, PATH, "getResolutions", true},

            // writes
            {ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", true},
            {ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithParent", true},
            {ScopeName.WRITE, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", true},
            {ScopeName.WRITE, HttpMethod.POST, PATH, "progressWorkflowAction", true},
            {ScopeName.WRITE, HttpMethod.POST, PATH, "updateIssue", true},
            {ScopeName.WRITE, HttpMethod.POST, PATH, "createIssue", true},

            // implied scopes
            {ScopeName.WRITE, HttpMethod.POST, PATH, "getIssue", true},
            {ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "getIssue", true},
            {ScopeName.ADMIN, HttpMethod.POST, PATH, "getIssue", true},
            {ScopeName.PROJECT_ADMIN, HttpMethod.POST, PATH, "updateIssue", true},
            {ScopeName.ADMIN, HttpMethod.POST, PATH, "updateIssue", true},

            // one thing wrong
            {ScopeName.READ, HttpMethod.POST, PATH, "does_not_exist", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParentWithSecurityLevel", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithParent", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "createIssueWithSecurityLevel", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "progressWorkflowAction", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "updateIssue", false},
            {ScopeName.READ, HttpMethod.POST, PATH, "createIssue", false},
            {null, HttpMethod.POST, PATH, "getIssue", false},
            {ScopeName.READ, HttpMethod.PUT, PATH, "getIssue", false},
            {ScopeName.READ, HttpMethod.DELETE, PATH, "getIssue", false},
        });
    }

    public JiraSoapRpcScopesTest(ScopeName scope, HttpMethod method, String path, String rpcMethod, boolean expectedOutcome)
    {
        super(scope, method, path, createSoapRpcPayload(rpcMethod), expectedOutcome, "/jira", "Jira");
    }

    private static String createSoapRpcPayload(String methodName)
    {
        return "<?xml version=\"1.0\"?>\n" +
                "<soap:Envelope\n" +
                "xmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\"\n" +
                "soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">\n" +
                "\n" +
                "<soap:Body xmlns:m=\"http://soap.rpc.jira.atlassian.com\">" +
                "    <m:" + methodName + ">" +
                "    </m:" + methodName + ">" +
                "</soap:Body>\n" +
                "\n" +
                "</soap:Envelope>";
    }
}
