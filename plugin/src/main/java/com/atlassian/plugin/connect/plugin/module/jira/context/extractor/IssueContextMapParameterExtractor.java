package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public class IssueContextMapParameterExtractor extends AbstractJiraContextMapParameterExtractor<Issue>
{
    private static final String ISSUE_CONTEXT_KEY = "issue";

    public IssueContextMapParameterExtractor(IssueSerializer issueSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        super(issueSerializer, ISSUE_CONTEXT_KEY, permissionManager, userManager);
    }

    @Override
    protected boolean hasPermission(PermissionManager permissionManager, ApplicationUser user, Issue issue, int permissionId)
    {
        return permissionManager.hasPermission(permissionId, issue, user);
    }

}
