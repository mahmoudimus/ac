package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public class IssueWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    private static final String ISSUE_CONTEXT_KEY = "issue";

    @Override
    public Map<String, Object> extract(final Map<String, Object> context)
    {
        if (context.containsKey(ISSUE_CONTEXT_KEY))
        {
            Issue issue = (Issue) context.get(ISSUE_CONTEXT_KEY);
            if (null != issue)
            {
                return ImmutableMap.<String, Object>of("issue", ImmutableMap.of(
                        "id", issue.getId(),
                        "key", issue.getKey()
                ));
            }
        }
        return Collections.emptyMap();
    }
}
