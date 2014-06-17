package it.com.atlassian.plugin.connect.workflow.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowProgressAware;
import com.google.common.collect.Maps;

import java.util.Map;

public class WorkflowAction implements WorkflowProgressAware
{
    private final ApplicationUser applicationUser;
    private final MutableIssue issue;
    private final ErrorCollection errorCollection;
    private final Map additionalInputs;
    private int action;

    public WorkflowAction(ApplicationUser applicationUser, MutableIssue issue, int action)
    {
        this.applicationUser = applicationUser;
        this.issue = issue;
        this.action = action;
        this.errorCollection = new SimpleErrorCollection();
        this.additionalInputs = Maps.newHashMap();
    }

    @Override
    public User getRemoteUser()
    {
        return applicationUser.getDirectoryUser();
    }

    @Override
    public ApplicationUser getRemoteApplicationUser()
    {
        return applicationUser;
    }

    @Override
    public int getAction()
    {
        return action;
    }

    @Override
    public void setAction(int action)
    {
        this.action = action;
    }

    @Override
    public void addErrorMessage(String message)
    {
        errorCollection.addErrorMessage(message);
    }

    @Override
    public void addError(String field, String message)
    {
        errorCollection.addError(field, message);
    }

    @Override
    public Map getAdditionalInputs()
    {
        return additionalInputs;
    }

    @Override
    public MutableIssue getIssue()
    {
        return issue;
    }

    @Override
    public Project getProject()
    {
        return issue.getProjectObject();
    }

    @Override
    public Project getProjectObject()
    {
        return issue.getProjectObject();
    }
}
