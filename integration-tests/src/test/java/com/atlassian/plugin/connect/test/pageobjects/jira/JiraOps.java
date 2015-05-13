package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.tests.TestBase;
import com.google.gson.JsonParser;
import org.apache.commons.lang.RandomStringUtils;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;

import java.util.Locale;

public class JiraOps
{
    final Backdoor backdoor;
    public JiraOps()
    {
        backdoor = TestBase.funcTestHelper.backdoor;
    }

    public String createProject() throws java.rmi.RemoteException
    {
        String key = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        backdoor.project().addProject("Test project " + key, key, "admin");
        return key;
    }
    
    public int getProjectId(String projectKey)
    {
        String response = backdoor.rawRestApiControl().rootResource().path("/project/" + projectKey).get(String.class);
        JsonParser parser = new JsonParser();
        return parser.parse(response).getAsJsonObject().get("id").getAsInt();
    }

    public void deleteProject(String projectKey) throws java.rmi.RemoteException
    {
        backdoor.project().deleteProject(projectKey);
    }

    public IssueCreateResponse createIssue(String projectKey, String summary) throws
            java.rmi.RemoteException
    {
        return backdoor.issues().createIssue(projectKey, summary);
    }

    public void setIssueSummary(String issueKey, String summary) throws
            java.rmi.RemoteException
    {
        backdoor.issues().setSummary(issueKey, summary);
    }

    public void transitionIssue(String issueKey)
            throws java.rmi.RemoteException
    {
        backdoor.rawRestApiControl().rootResource().path("/issue/" + issueKey + "/transitions")
                .post("{\"transition\": {\"id\": \"5\"}}");
    }
}
