package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JiraComponent
public class ProjectIdContextParameter implements ProjectContextParameterMapper.ProjectParameter
{

    private static final String PARAMETER_KEY = "project.id";

    private JiraAuthenticationContext authenticationContext;
    private ProjectService projectService;

    @Autowired
    public ProjectIdContextParameter(JiraAuthenticationContext authenticationContext,
            ProjectService projectService)
    {
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Project contextValue)
    {
        return isProjectAccessibleByCurrentUser(contextValue.getId());
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return isProjectAccessibleByCurrentUser(Long.valueOf(value));
    }

    private boolean isProjectAccessibleByCurrentUser(Long projectId)
    {
        ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();
        return projectService.getProjectById(loggedInUser, projectId).isValid();
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Project contextValue)
    {
        return Long.toString(contextValue.getId());
    }
}
