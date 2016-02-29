package com.atlassian.plugin.connect.confluence.web.spacetools;

import com.atlassian.confluence.spaces.actions.SpaceAdminAction;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.confluence.web.context.ConfluenceModuleContextParameters;
import com.atlassian.plugin.connect.confluence.web.context.ConfluenceModuleContextParametersImpl;

import java.io.IOException;
import java.util.Collections;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;
import static java.util.Collections.emptyMap;

public class SpaceToolsIFrameAction extends SpaceAdminAction {
    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private ModuleContextFilter moduleContextFilter;
    private SpaceToolsTabContext context;

    public String getIFrameHtml() throws IOException {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(context.getAddonKey(), context.getModuleKey());

        if (renderStrategy.shouldShow(Collections.<String, Object>emptyMap())) {
            ConfluenceModuleContextParameters unfilteredContext = new ConfluenceModuleContextParametersImpl(emptyMap());
            unfilteredContext.addSpace(this.space);
            ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);
            return renderToString(filteredContext, renderStrategy);
        } else {
            return renderAccessDeniedToString(renderStrategy);
        }
    }

    public SpaceToolsTabContext getSpaceTabInfo() {
        return context;
    }

    public String getSpaceAdminWebItemKey() {
        return context.getSpaceAdminWebItemKey();
    }

    public String getSpaceToolsWebItemKey() {
        return context.getModuleKey();
    }

    public String getTitle() {
        return context.getDisplayName();
    }

    public void setiFrameRenderStrategyRegistry(final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry) {
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    public void setModuleContextFilter(final ModuleContextFilter moduleContextFilter) {
        this.moduleContextFilter = moduleContextFilter;
    }

    public void provideContext(SpaceToolsTabContext context) {
        this.context = context;
    }
}
