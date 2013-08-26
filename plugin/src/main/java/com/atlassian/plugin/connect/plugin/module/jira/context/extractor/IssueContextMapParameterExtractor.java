package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public class IssueContextMapParameterExtractor implements ContextMapParameterExtractor<Issue>
{
    private static final String ISSUE_CONTEXT_KEY = "issue";
    private IssueSerializer issueSerializer;

    public IssueContextMapParameterExtractor(IssueSerializer issueSerializer)
    {
        this.issueSerializer = issueSerializer;
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

}
