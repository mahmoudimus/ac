package com.atlassian.plugin.connect.plugin.module.jira.componenttab;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractIFrameTab;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;

/**
 * A component tab that is displayed as an iframe
 */
public class IFrameComponentTab extends AbstractIFrameTab<ComponentTabPanelModuleDescriptor, BrowseComponentContext> implements ComponentTabPanel
{
    public IFrameComponentTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Condition condition, UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(urlVariableSubstitutor, iFrameContext, iFrameRenderer, condition);
    }

    @Override
    protected Map<String, Object> getParams(final BrowseComponentContext context)
    {
        return ImmutableMap.<String, Object>of(
                "component",
                    ImmutableMap.of("id", context.getComponent().getId()),
                "project",
                    ImmutableMap.of("id", context.getProject().getId(),
                        "key", context.getProject().getKey()));
    }

}
