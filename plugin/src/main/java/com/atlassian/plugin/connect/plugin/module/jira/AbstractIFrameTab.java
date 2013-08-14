package com.atlassian.plugin.connect.plugin.module.jira;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;

/**
 * Common part of tab panel for all JIRA tab panels
 */
public abstract class AbstractIFrameTab<D, C extends BrowseContext>
{

    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final IFrameContext iFrameContext;
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;

    public AbstractIFrameTab(UrlVariableSubstitutor urlVariableSubstitutor, IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Condition condition) {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
    }

    protected abstract Map<String, Object> getParams(final C context);

    public void init(D descriptor)
    {
    }

    public String getHtml(final C context)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            writer.write(iFrameRenderer.render(substituteContext(getParams(context)),
                    getUserName(context)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    public boolean showPanel(final C context)
    {
        return condition == null || condition.shouldDisplay(createConditionContext(context));
    }

    protected IFrameContext substituteContext(final Map<String, Object> paramsMap)
    {
        final String urlWithSubstitutedParameters = urlVariableSubstitutor.replace(iFrameContext.getIframePath(), paramsMap);

        return new IFrameContextImpl(iFrameContext.getPluginKey(), urlWithSubstitutedParameters, iFrameContext.getNamespace(), iFrameContext.getIFrameParams());
    }

    private String getUserName(final BrowseContext browseContext)
    {
        return browseContext.getUser() != null ? browseContext.getUser().getName() : null;
    }

}
