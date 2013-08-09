package com.atlassian.plugin.connect.plugin.module.jira.componenttab;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
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
 *
 * @since v6.1
 */
public class IFrameComponentTab implements ComponentTabPanel
{
    private final IFrameContext iFrameContext;
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private static final Logger log = LoggerFactory.getLogger(IFrameComponentTab.class);

    public IFrameComponentTab(final IFrameContext iFrameContext, final IFrameRendererImpl iFrameRenderer, final Condition condition, final UrlVariableSubstitutor urlVariableSubstitutor) {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }


    @Override
    public void init(final ComponentTabPanelModuleDescriptor componentTabPanelModuleDescriptor)
    {
    }

    @Override
    public String getHtml(final BrowseComponentContext browseContext)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            final String remoteUser = getUserName(browseContext);
            writer.write(iFrameRenderer.render(substituteContext(browseContext), remoteUser));
        }
        catch (IOException e)
        {
            log.error("Error rendering tab", e);
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private IFrameContext substituteContext(final BrowseComponentContext browseContext)
    {
        final Map<String, Object> paramsMap = ImmutableMap.<String, Object>of(
                "component",
                ImmutableMap.of("id", browseContext.getComponent().getId()),
                "project",
                ImmutableMap.of("id", browseContext.getProject().getId(),
                        "key", browseContext.getProject().getKey()));

        final String urlWithSubstitutedParameters = urlVariableSubstitutor.replace(iFrameContext.getIframePath(), paramsMap);

        return new IFrameContextImpl(iFrameContext.getPluginKey(), urlWithSubstitutedParameters, iFrameContext.getNamespace(), iFrameContext.getIFrameParams());
    }

    @Override
    public boolean showPanel(final BrowseComponentContext browseContext)
    {
        return condition == null || condition.shouldDisplay(createConditionContext(browseContext));
    }

    private String getUserName(final BrowseContext browseContext)
    {
        return browseContext.getUser() != null ? browseContext.getUser().getName() : null;
    }
}
