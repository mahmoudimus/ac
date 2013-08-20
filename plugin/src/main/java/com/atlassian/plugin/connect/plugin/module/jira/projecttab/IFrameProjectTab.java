package com.atlassian.plugin.connect.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractIFrameTab;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * A project tab that is displayed as an iframe
 */
public class IFrameProjectTab extends AbstractIFrameTab<ProjectTabPanelModuleDescriptor, BrowseContext> implements ProjectTabPanel
{
    public IFrameProjectTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Condition condition, UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(urlVariableSubstitutor, iFrameContext, iFrameRenderer, condition);
    }

    @Override
    protected Map<String, Object> getParams(final BrowseContext context)
    {
        return ImmutableMap.<String, Object>of(
                "project",
                    ImmutableMap.of(
                            "id", context.getProject().getId(),
                            "key", context.getProject().getKey()
                    ),
                /* //deprecated: use project.key instead; to be removed with AC-702  */
                "ctx_project_key", context.getContextKey(),
                /* //deprecated: use project.id instead; to be removed with AC-702  */
                "ctx_project_id", String.valueOf(context.getProject().getId())
        );
    }
}
