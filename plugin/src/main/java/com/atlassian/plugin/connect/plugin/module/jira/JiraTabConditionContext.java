package com.atlassian.plugin.connect.plugin.module.jira;

import java.util.Map;

import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.ExecutingHttpRequest;

import com.google.common.collect.ImmutableMap;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USER;

/**
 * Unfortunately, {@link com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition} and
 * {@link com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition} expect the context to conform
 * to a specific contract.
 * This utility class creates contexts for conditions executed by JIRA tabs.
 */
public final class JiraTabConditionContext
{
    private JiraTabConditionContext() {}

    public static Map<String, Object> createConditionContext(ShowPanelRequest request)
    {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(),
                request.issue().getProjectObject(),
                ImmutableMap.<String, Object>of("issue", request.issue()));
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(CONTEXT_KEY_HELPER, helper);
        if (request.remoteUser() != null)
        {
            builder.put(CONTEXT_KEY_USER, request.remoteUser());
        }
        return builder.build();
    }

    public static Map<String, Object> createConditionContext(BrowseContext browseContext)
    {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(),
                browseContext.getProject(), browseContext.createParameterMap());
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(CONTEXT_KEY_HELPER, helper);
        if (browseContext.getUser() != null)
        {
            builder.put(CONTEXT_KEY_USER, browseContext.getUser());
        }
        return builder.build();
    }
}
