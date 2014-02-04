package com.atlassian.plugin.connect.plugin.iframe.context.jira;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.sal.api.user.UserProfile;

/**
 *
 */
public interface JiraModuleContextParameters extends ModuleContextParameters
{
    void addIssue(Issue issue);
    void addProject(Project project);
    void addVersion(Version version);
    void addComponent(ProjectComponent projectComponent, Project project);
    void addProfileUser(UserProfile userProfile);
}
