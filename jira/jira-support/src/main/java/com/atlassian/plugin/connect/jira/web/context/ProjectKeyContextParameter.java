package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JiraComponent
public class ProjectKeyContextParameter implements ProjectContextParameterMapper.ProjectParameter
{

    private static final String PARAMETER_KEY = "project.key";

    private JiraAuthenticationContext authenticationContext;
    private ProjectService projectService;

    @Autowired
    public ProjectKeyContextParameter(JiraAuthenticationContext authenticationContext,
            ProjectService projectService)
    {
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Project contextValue)
    {
        return isProjectAccessibleByCurrentUser(contextValue.getKey());
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return isProjectAccessibleByCurrentUser(value);
    }

    private boolean isProjectAccessibleByCurrentUser(String projectKey)
    {
        ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();
        return projectService.getProjectByKey(loggedInUser, projectKey).isValid();
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(Project contextValue)
    {
        return contextValue.getKey();
    }
}
