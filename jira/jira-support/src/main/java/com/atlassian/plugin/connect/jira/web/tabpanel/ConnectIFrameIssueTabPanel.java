package com.atlassian.plugin.connect.jira.web.tabpanel;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParametersImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USERNAME;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;
import static java.util.Collections.emptyMap;

public class ConnectIFrameIssueTabPanel extends AbstractIssueTabPanel3 {
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final ModuleContextFilter moduleContextFilter;

    public ConnectIFrameIssueTabPanel(IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter) {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request) {
        Map<String, Object> conditionContext = Maps.newHashMap();
        populateConditionContext(conditionContext, request);
        return iFrameRenderStrategy.shouldShow(conditionContext);
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request) {
        // parse and filter module context
        JiraModuleContextParameters unfilteredContext = createUnfilteredContext(request.issue());
        ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);

        // render tab HTML
        StringIssueAction stringAction = new StringIssueAction(renderToString(filteredContext, iFrameRenderStrategy));

        return Lists.<IssueAction>newArrayList(stringAction);
    }

    protected void populateConditionContext(Map<String, Object> conditionContext, ShowPanelRequest request) {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(), request.issue().getProjectObject(),
                ImmutableMap.<String, Object>of("issue", request.issue()));
        conditionContext.put(CONTEXT_KEY_HELPER, helper);
        if (!request.isAnonymous()) {
            ApplicationUser user = request.remoteUser();
            if (user != null) {
                conditionContext.put(CONTEXT_KEY_USERNAME, user.getUsername());
            }
        }
        conditionContext.put(MODULE_CONTEXT_KEY, createUnfilteredContext(request.issue()));
    }

    private JiraModuleContextParameters createUnfilteredContext(final Issue issue) {
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl(emptyMap());
        unfilteredContext.addIssue(issue);
        return unfilteredContext;
    }
}
