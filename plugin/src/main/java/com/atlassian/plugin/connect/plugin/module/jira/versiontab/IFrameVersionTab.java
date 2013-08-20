package com.atlassian.plugin.connect.plugin.module.jira.versiontab;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractIFrameTab;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;

/**
 * A version tab that is displayed as an iframe
 */
public class IFrameVersionTab extends AbstractIFrameTab<VersionTabPanelModuleDescriptor, BrowseVersionContext> implements VersionTabPanel
{
    public IFrameVersionTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer,
            Condition condition, UrlVariableSubstitutor urlVariableSubstitutor)
    {
        super(urlVariableSubstitutor, iFrameContext, iFrameRenderer, condition);
    }

    @Override
    protected Map<String, Object> getParams(final BrowseVersionContext browseVersionContext)
    {
        return ImmutableMap.<String, Object>of(
                "version",
                    ImmutableMap.of("id", browseVersionContext.getVersion().getId()),
                "project",
                    ImmutableMap.of("id", browseVersionContext.getProject().getId(),
                        "key", browseVersionContext.getProject().getKey()));
    }


}
