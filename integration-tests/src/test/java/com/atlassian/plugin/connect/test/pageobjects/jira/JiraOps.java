package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.ProductInstance;
import hudson.plugins.jira.soap.JiraSoapService;
import hudson.plugins.jira.soap.JiraSoapServiceServiceLocator;
import hudson.plugins.jira.soap.RemoteFieldValue;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteNamedObject;
import hudson.plugins.jira.soap.RemoteProject;
import org.apache.commons.lang.RandomStringUtils;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class JiraOps
{
    final JiraSoapService soap;
    final String token;

    public JiraOps(ProductInstance instance)
    {
        this(instance.getBaseUrl());
    }

    public JiraOps(String baseUrl)
    {
        JiraSoapService svc;
        try
        {
            svc = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(
                    baseUrl + "/rpc/soap/jirasoapservice-v2"));
            token = svc.login("admin", "admin");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        soap = svc;
    }

    public RemoteProject createProject() throws java.rmi.RemoteException
    {
        String key = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        return soap.createProject(token, key, "Test project " + key,
                "This is a test project " + key,
                null, "admin", null, null, null);

    }

    public void deleteProject(String key) throws java.rmi.RemoteException
    {
        soap.deleteProject(token, key);
    }

    public RemoteIssue createIssue(String projectKey, String summary) throws
            java.rmi.RemoteException
    {
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(projectKey);
        issue.setType("3"); // "task"
        issue.setSummary(summary);
        return soap.createIssue(token, issue);
    }

    public RemoteIssue updateIssue(String issueKey, Map<String, String> fields) throws
            java.rmi.RemoteException
    {
        List<RemoteFieldValue> values = newArrayList();
        for (Map.Entry<String, String> entry : fields.entrySet())
        {
            values.add(new RemoteFieldValue(entry.getKey(), new String[]{entry.getValue()}));
        }
        return soap.updateIssue(token, issueKey, values.toArray(new RemoteFieldValue[values.size()]));
    }

    public RemoteNamedObject[] availableActions(String issueKey) throws java.rmi.RemoteException
    {
        return soap.getAvailableActions(token, issueKey);
    }

    public RemoteIssue transitionIssue(String issueKey, String actionId, Map<String, String> fields)
            throws java.rmi.RemoteException
    {
        List<RemoteFieldValue> values = newArrayList();
        for (Map.Entry<String, String> entry : fields.entrySet())
        {
            values.add(new RemoteFieldValue(entry.getKey(), new String[]{entry.getValue()}));
        }
        return soap.progressWorkflowAction(token, issueKey, actionId, values.toArray(new RemoteFieldValue[values.size()]));
    }
}
