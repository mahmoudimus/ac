package com.atlassian.plugin.connect.plugin.iframe.tabpanel.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USER;
import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;

/**
 *
 */
public class ConnectIFrameIssueTabPanel extends AbstractIssueTabPanel3
{
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final ModuleContextFilter moduleContextFilter;

    public ConnectIFrameIssueTabPanel(IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request)
    {
        Map<String, Object> conditionContext = Maps.newHashMap();
        populateConditionContext(conditionContext, request);
        return iFrameRenderStrategy.shouldShow(conditionContext);
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request)
    {
        // parse and filter module context
        JiraModuleContextParameters unfilteredContext = createUnfilteredContext(request.issue());
        ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);

        // render tab HTML
        StringIssueAction stringAction = new StringIssueAction(renderToString(filteredContext, iFrameRenderStrategy));

        return Lists.<IssueAction>newArrayList(stringAction);
    }

    protected void populateConditionContext(Map<String, Object> conditionContext, ShowPanelRequest request)
    {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(), request.issue().getProjectObject(),
                ImmutableMap.<String, Object>of("issue", request.issue()));
        conditionContext.put(CONTEXT_KEY_HELPER, helper);
        if (!request.isAnonymous())
        {
            conditionContext.put(CONTEXT_KEY_USER, request.remoteUser());
        }
        conditionContext.put(MODULE_CONTEXT_KEY, createUnfilteredContext(request.issue()));
    }

    private JiraModuleContextParameters createUnfilteredContext(final Issue issue)
    {
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
        unfilteredContext.addIssue(issue);
        return unfilteredContext;
    }

}
