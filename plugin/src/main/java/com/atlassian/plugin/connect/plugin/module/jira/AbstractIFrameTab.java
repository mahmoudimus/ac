package com.atlassian.plugin.connect.plugin.module.jira;

import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;

/**
 * Common part of tab panel for all JIRA tab panels
 * @param <D> module descriptor class for local tab
 * @param <C> jira-core context given locally to renderer like BrowseVersionContext>
 */
public abstract class AbstractIFrameTab<D, C extends BrowseContext>
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final IFrameRenderer iFrameRenderer;

    private final IFrameContext iFrameContext;
    private final Condition condition;

    public AbstractIFrameTab(UrlVariableSubstitutor urlVariableSubstitutor, IFrameContext iFrameContext, IFrameRenderer iFrameRenderer, Condition condition)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
    }

    /**
     * Implement this method to retrieve data from context and provide them to iframe
     * @param context data context
     * @return map of params that will be provided to iframe
     */
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

    private IFrameContext substituteContext(final Map<String, Object> paramsMap)
    {
        final String urlWithSubstitutedParameters = urlVariableSubstitutor.replace(iFrameContext.getIframePath(), paramsMap);

        return new IFrameContextImpl(iFrameContext.getPluginKey(), urlWithSubstitutedParameters, iFrameContext.getNamespace(), iFrameContext.getIFrameParams());
    }

    private String getUserName(final BrowseContext browseContext)
    {
        return browseContext.getUser() != null ? browseContext.getUser().getName() : null;
    }

}
