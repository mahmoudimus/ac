package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.bc.issue.IssueService.IssueResult;

/**
 * Serializes Issue objects.
 */
public class IssueSerializer extends AbstractJiraParameterSerializer<Issue, IssueResult>
{

    public static final String ISSUE_FIELD_NAME = "issue";

    public IssueSerializer(final IssueService issueService, UserManager userManager)
    {
        super(userManager, ISSUE_FIELD_NAME,
                new ParameterUnwrapper<IssueResult, Issue>()
                {
                    @Override
                    public Issue unwrap(IssueResult wrapped)
                    {
                        return wrapped.getIssue();
                    }
                },
                new AbstractIdParameterLookup<IssueResult>()
                {
                    @Override
                    public IssueResult lookup(User user, Long id)
                    {
                        return issueService.getIssue(user, id);
                    }
                },
                new AbstractKeyParameterLookup<IssueResult>()
                {
                    @Override
                    public IssueResult lookup(User user, String key)
                    {
                        return issueService.getIssue(user, key);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final Issue issue)
    {
        return ImmutableMap.<String, Object>of(ISSUE_FIELD_NAME, ImmutableMap.of(
                ID_FIELD_NAME, issue.getId(),
                KEY_FIELD_NAME, issue.getKey()
        ));
    }

}
