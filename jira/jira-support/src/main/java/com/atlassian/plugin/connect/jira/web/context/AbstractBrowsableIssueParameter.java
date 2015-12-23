package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

public abstract class AbstractBrowsableIssueParameter implements IssueContextParameterMapper.IssueParameter
{

    private JiraAuthenticationContext authenticationContext;
    private PermissionManager permissionManager;

    public AbstractBrowsableIssueParameter(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean isAccessibleByCurrentUser(Issue contextValue)
    {
        ApplicationUser loggedInUser = authenticationContext.getLoggedInUser();
        return permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, contextValue, loggedInUser);
    }
}
