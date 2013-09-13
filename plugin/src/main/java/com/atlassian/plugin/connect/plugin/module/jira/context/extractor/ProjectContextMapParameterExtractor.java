package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;

/**
 * Extracts project parameters that can be included in webpanel's iframe url.
 */
public class ProjectContextMapParameterExtractor extends AbstractJiraContextMapParameterExtractor<Project>
{
    private static final String PROJECT_CONTEXT_KEY = "project";

    public ProjectContextMapParameterExtractor(ProjectSerializer issueSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        super(issueSerializer, PROJECT_CONTEXT_KEY, permissionManager, userManager);
    }

    @Override
    protected boolean hasPermission(PermissionManager permissionManager, ApplicationUser user, Project project, int permissionId)
    {
        return permissionManager.hasPermission(permissionId, project, user);
    }

}
