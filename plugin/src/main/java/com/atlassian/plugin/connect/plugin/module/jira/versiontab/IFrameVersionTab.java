package com.atlassian.plugin.connect.plugin.module.jira.versiontab;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;

/**
 * A tab that displays an iframe
 */
public class IFrameVersionTab implements VersionTabPanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameVersionTab.class);

    private final IFrameContext iFrameContext;
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    public IFrameVersionTab(final IFrameContext iFrameContext, final IFrameRendererImpl iFrameRenderer,
            final Condition condition, final UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    @Override
    public void init(final VersionTabPanelModuleDescriptor tabPanelModuleDescriptor)
    {
    }

    @Override
    public String getHtml(final BrowseVersionContext browseContext)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            writer.write(iFrameRenderer.render(substituteContext(browseContext),
                    getUserName(browseContext)));
        }
        catch (IOException e)
        {
            log.error("Error rendering tab", e);
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private IFrameContext substituteContext(final BrowseVersionContext browseContext)
    {
        final Map<String, Object> paramsMap = ImmutableMap.<String, Object>of(
                "version",
                ImmutableMap.of("id", browseContext.getVersion().getId()),
                "project",
                ImmutableMap.of("id", browseContext.getProject().getId(),
                        "key", browseContext.getProject().getKey()));

        final String urlWithSubstitutedParameters = urlVariableSubstitutor.replace(iFrameContext.getIframePath(), paramsMap);

        return new IFrameContextImpl(iFrameContext.getPluginKey(), urlWithSubstitutedParameters, iFrameContext.getNamespace(), iFrameContext.getIFrameParams());
    }

    private String getUserName(final BrowseVersionContext browseContext)
    {
        return browseContext.getUser() != null ? browseContext.getUser().getName() : null;
    }

    @Override
    public boolean showPanel(final BrowseVersionContext browseContext)
    {
        return condition == null || condition.shouldDisplay(createConditionContext(browseContext));
    }
}
