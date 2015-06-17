package com.atlassian.plugin.connect.jira.iframe.tabpanel.project;

import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParametersImpl;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USER;
import static com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.spi.iframe.webpanel.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;

/**
 *
 */
public abstract class AbstractConnectIFrameTabPanel<D extends TabPanelModuleDescriptor, C extends BrowseContext> implements TabPanel<D, C>
{
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final ModuleContextFilter moduleContextFilter;

    protected AbstractConnectIFrameTabPanel(IFrameRenderStrategy iFrameRenderStrategy, ModuleContextFilter moduleContextFilter)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public void init(final D descriptor)
    {
    }

    @Override
    public String getHtml(final C ctx)
    {
        // parse and filter module context
        JiraModuleContextParameters unfilteredContext = createUnfilteredContext(ctx);
        ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);

        // render tab HTML
        return renderToString(filteredContext, iFrameRenderStrategy);
    }

    private JiraModuleContextParameters createUnfilteredContext(final C ctx)
    {
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
        populateModuleContext(unfilteredContext, ctx);
        return unfilteredContext;
    }

    @Override
    public boolean showPanel(final C ctx)
    {
        // create context for evaluating conditions
        Map<String, Object> conditionContext = Maps.newHashMap();
        populateConditionContext(conditionContext, ctx);

        // evaluate condition
        return iFrameRenderStrategy.shouldShow(conditionContext);
    }

    protected abstract void populateModuleContext(final JiraModuleContextParameters moduleContext, final C ctx);

    protected void populateConditionContext(Map<String, Object> conditionContext, C ctx)
    {
        JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(), ctx.getProject(), ctx.createParameterMap());
        conditionContext.put(CONTEXT_KEY_HELPER, helper);
        if (ctx.getUser() != null)
        {
            conditionContext.put(CONTEXT_KEY_USER, ctx.getUser());
        }
        conditionContext.put(MODULE_CONTEXT_KEY, createUnfilteredContext(ctx));
    }

}
