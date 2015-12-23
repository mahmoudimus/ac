package com.atlassian.plugin.connect.confluence.web.spacetools;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.confluence.web.context.SpaceContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;

public class SpaceToolsIFrameAction extends SpaceAdminAction
{
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private PluggableParametersExtractor pluggableParametersExtractor;
    private SpaceContextParameterMapper spaceContextParameterMapper;
    private SpaceToolsTabContext context;

    public String getIFrameHtml() throws IOException
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(context.getAddOnKey(), context.getModuleKey());

        if (renderStrategy.shouldShow(Collections.<String, Object>emptyMap()))
        {
            return renderToString(buildContextParameters(), renderStrategy);
        }
        else
        {
            return renderAccessDeniedToString(renderStrategy);
        }
    }

    public SpaceToolsTabContext getSpaceTabInfo()
    {
        return context;
    }

    public String getSpaceAdminWebItemKey()
    {
        return context.getSpaceAdminWebItemKey();
    }

    public String getSpaceToolsWebItemKey()
    {
        return context.getModuleKey();
    }

    public String getTitle()
    {
        return context.getDisplayName();
    }

    public void setiFrameRenderStrategyRegistry(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    public void setPluggableParametersExtractor(PluggableParametersExtractor pluggableParametersExtractor)
    {
        this.pluggableParametersExtractor = pluggableParametersExtractor;
    }

    public void setSpaceContextParameterMapper(SpaceContextParameterMapper spaceContextParameterMapper)
    {
        this.spaceContextParameterMapper = spaceContextParameterMapper;
    }

    public void provideContext(SpaceToolsTabContext context)
    {
        this.context = context;
    }

    private Map<String, String> buildContextParameters()
    {
        Map<String, Object> context = Maps.newHashMap();
        TypeBasedConnectContextParameterMapper.addContextEntry(spaceContextParameterMapper, space, context);
        return pluggableParametersExtractor.extractParameters(context);
    }
}
