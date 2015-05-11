package com.atlassian.plugin.connect.jira.iframe.context;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

/**
 * @since 1.0
 */
public interface JiraModuleContextParameters extends ModuleContextParameters
{
    void addIssue(Issue issue);
    void addProject(Project project);
    void addVersion(Version version);
    void addComponent(ProjectComponent projectComponent, Project project);
}
