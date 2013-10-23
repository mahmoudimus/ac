package com.atlassian.plugin.connect.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractIFrameTab;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A project tab that is displayed as an iframe
 */
public class IFrameProjectTab extends AbstractIFrameTab<ProjectTabPanelModuleDescriptor, BrowseContext> implements ProjectTabPanel
{
    private final ProjectSerializer projectSerializer;

    public IFrameProjectTab(IFrameContext iFrameContext, IFrameRenderer iFrameRenderer, Condition condition, UrlVariableSubstitutor urlVariableSubstitutor, ProjectSerializer projectSerializer)
    {
        super(urlVariableSubstitutor, iFrameContext, iFrameRenderer, condition);
        this.projectSerializer = projectSerializer;
    }

    @Override
    protected Map<String, Object> getParams(final BrowseContext context)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(projectSerializer.serialize(context.getProject()));
        // deprecated AC-702
        builder.put("ctx_project_key", context.getContextKey());
        builder.put("ctx_project_id", String.valueOf(context.getProject().getId()));
        return builder.build();
    }
}
