package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.google.common.base.Optional;

import java.util.Map;

import static com.atlassian.jira.security.Permissions.USE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public class IssueContextMapParameterExtractor implements ContextMapParameterExtractor<Issue>
{
    private static final String ISSUE_CONTEXT_KEY = "issue";
    private IssueSerializer issueSerializer;
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public IssueContextMapParameterExtractor(IssueSerializer issueSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        this.issueSerializer = issueSerializer;
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }

    @Override
    public Optional<Issue> extract(final Map<String, Object> context)
    {
        if (context.containsKey(ISSUE_CONTEXT_KEY))
        {
            Issue issue = (Issue) context.get(ISSUE_CONTEXT_KEY);
            return Optional.fromNullable(issue);
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<Issue> serializer()
    {
        return issueSerializer;
    }

    @Override
    public boolean hasViewPermission(String username, Issue issue)
    {
        return permissionManager.hasPermission(USE, issue, userManager.getUserByName(username));
    }

}
