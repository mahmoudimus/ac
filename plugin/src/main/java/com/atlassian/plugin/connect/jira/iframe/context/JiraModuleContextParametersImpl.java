package com.atlassian.plugin.connect.jira.iframe.context;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.connect.spi.iframe.context.HashMapModuleContextParameters;

/**
 *
 */
public class JiraModuleContextParametersImpl extends HashMapModuleContextParameters implements JiraModuleContextParameters
{
    @Override
    public void addIssue(final Issue issue)
    {
        if (issue != null)
        {
            put(JiraModuleContextFilter.ISSUE_KEY, issue.getKey());
            put(JiraModuleContextFilter.ISSUE_ID, Long.toString(issue.getId()));
            put(JiraModuleContextFilter.ISSUETYPE_ID, issue.getIssueTypeId());
            addProject(issue.getProjectObject());
        }
    }

    @Override
    public void addVersion(final Version version)
    {
        if (version != null)
        {
            put(JiraModuleContextFilter.VERSION_ID, Long.toString(version.getId()));
            addProject(version.getProjectObject());
        }
    }

    @Override
    public void addComponent(final ProjectComponent component, final Project project)
    {
        if (component != null)
        {
            put(JiraModuleContextFilter.COMPONENT_ID, Long.toString(component.getId()));
            addProject(project);
        }
    }

    @Override
    public void addProject(final Project project)
    {
        if (project != null)
        {
            put(JiraModuleContextFilter.PROJECT_KEY, project.getKey());
            put(JiraModuleContextFilter.PROJECT_ID, Long.toString(project.getId()));
        }
    }
}
