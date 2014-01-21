package com.atlassian.plugin.connect.plugin.iframe.context.jira;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.sal.api.user.UserProfile;

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

    @Override
    public void addProfileUser(final UserProfile userProfile)
    {
        if (userProfile != null)
        {
            put(JiraModuleContextFilter.PROFILE_NAME, userProfile.getUsername());
            put(JiraModuleContextFilter.PROFILE_KEY, userProfile.getUserKey().getStringValue());
        }
    }
}
