package com.atlassian.plugin.connect.plugin.module.jira.versiontab;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractIFrameTab;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.VersionSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A version tab that is displayed as an iframe
 */
public class IFrameVersionTab extends AbstractIFrameTab<VersionTabPanelModuleDescriptor, BrowseVersionContext> implements VersionTabPanel
{
    private final VersionSerializer versionSerializer;
    private final ProjectSerializer projectSerializer;

    public IFrameVersionTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer,
            Condition condition, UrlVariableSubstitutor urlVariableSubstitutor, ProjectSerializer projectSerializer,
            VersionSerializer versionSerializer)
    {
        super(urlVariableSubstitutor, iFrameContext, iFrameRenderer, condition);
        this.projectSerializer = projectSerializer;
        this.versionSerializer = versionSerializer;
    }

    @Override
    protected Map<String, Object> getParams(final BrowseVersionContext browseVersionContext)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(projectSerializer.serialize(browseVersionContext.getProject()));
        builder.putAll(versionSerializer.serialize(browseVersionContext.getVersion()));
        return builder.build();
    }


}
