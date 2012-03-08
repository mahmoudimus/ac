package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.pageobjects.ProductInstance;
import hudson.plugins.jira.soap.*;
import org.apache.commons.lang.RandomStringUtils;

import java.net.URL;
import java.util.Locale;

public class JiraOps
{
    final JiraSoapService soap;
    final String token;
    public JiraOps(ProductInstance instance)
    {
        JiraSoapService svc;
        try
        {
            svc = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(
                    instance.getBaseUrl() + "/rpc/soap/jirasoapservice-v2"));
            token = svc.login("admin", "admin");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        soap = svc;
    }
    public RemoteProject createProject() throws java.rmi.RemoteException, RemoteValidationException,
                                              RemoteAuthenticationException
    {
        String key = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
        return soap.createProject(token, key, "Test project " + key, "This is a test project " + key,
                null, "admin", soap.getPermissionSchemes(token)[0], null, null);

    }
    
    public void deleteProject(String key) throws java.rmi.RemoteException,
                                                        RemoteAuthenticationException
    {
        soap.deleteProject(token, key);
    }
    
    public RemoteIssue createIssue(String projectKey, String summary) throws
                                                                             java.rmi.RemoteException,
                                                                             
                                                                             
                                                                             RemoteValidationException,
                                                                             RemoteAuthenticationException
    {
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(projectKey);
        issue.setType("1");
        issue.setSummary(summary);
        return soap.createIssue(token, issue);
    }
}
