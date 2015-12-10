package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.connect.spi.web.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.spi.web.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
@JiraComponent
public class IssueContextMapParameterExtractor implements ContextMapParameterExtractor<Issue>
{
    private static final String ISSUE_CONTEXT_KEY = "issue";
    private IssueSerializer issueSerializer;

    @Autowired
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
            return Optional.ofNullable(issue);
        }
        return Optional.empty();
    }

    @Override
    public ParameterSerializer<Issue> serializer()
    {
        return issueSerializer;
    }

}
