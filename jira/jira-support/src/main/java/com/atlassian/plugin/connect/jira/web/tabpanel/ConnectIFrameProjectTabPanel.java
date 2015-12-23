package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;

import java.util.Map;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;

public class ConnectIFrameProjectTabPanel implements ProjectTabPanel
{

    private IFrameRenderStrategy iFrameRenderStrategy;
    private PluggableParametersExtractor pluggableParametersExtractor;

    public ConnectIFrameProjectTabPanel(final IFrameRenderStrategy iFrameRenderStrategy,
            PluggableParametersExtractor pluggableParametersExtractor)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
    }

    @Override
    public void init(final ProjectTabPanelModuleDescriptor descriptor)
    {}

    @Override
    public boolean showPanel(final BrowseContext browseContext)
    {
        Map<String, Object> context = browseContext.createParameterMap();
        return iFrameRenderStrategy.shouldShow(context);
    }

    @Override
    public String getHtml(final BrowseContext browseContext)
    {
        Map<String, Object> context = browseContext.createParameterMap();
        Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(context);
        return renderToString(contextParameters, iFrameRenderStrategy);
    }
}
