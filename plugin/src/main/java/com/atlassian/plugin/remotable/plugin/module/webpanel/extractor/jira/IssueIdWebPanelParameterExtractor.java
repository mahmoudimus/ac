package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts issue id that will be included in webpanel's iframe url.
 */
public class IssueIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    private static final String ISSUE_CONTEXT_KEY = "issue";
    public static final String ISSUE_ID = "issue_id";

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        if (context.containsKey(ISSUE_CONTEXT_KEY))
        {
            Long issueId = ((Issue) context.get(ISSUE_CONTEXT_KEY)).getId();
            return Optional.<Map.Entry<String, String[]>>of(new ImmutableWebPanelParameterPair(ISSUE_ID, new String[] { String.valueOf(issueId) }));
        }
        else
        {
            return Optional.absent();
        }
    }
}
