package com.atlassian.plugin.connect.jira.iframe.tabpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

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
        return createContext(helper, request.remoteUser());
    }

    public static Map<String, Object> createConditionContext(BrowseContext browseContext)
    {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(),
                browseContext.getProject(), browseContext.createParameterMap());
        return createContext(helper, browseContext.getUser());
    }

    private static Map<String, Object> createContext(JiraHelper helper, User user)
    {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(CONTEXT_KEY_HELPER, helper);
        if (user != null)
        {
            builder.put(CONTEXT_KEY_USER, user);
        }
        return builder.build();
    }
}
