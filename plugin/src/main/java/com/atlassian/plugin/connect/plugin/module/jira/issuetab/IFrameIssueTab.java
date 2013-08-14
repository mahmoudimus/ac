package com.atlassian.plugin.connect.plugin.module.jira.issuetab;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.plugin.issuetabpanel.*;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.plugin.module.jira.JiraTabConditionContext.createConditionContext;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An issue tab that displays an iframe but isn't included in the all tab
 */
public class IFrameIssueTab extends AbstractIssueTabPanel3
{
    private static final Logger log = LoggerFactory.getLogger(IFrameIssueTab.class);
    private final IFrameRendererImpl iFrameRenderer;
    private final Optional<Condition> condition;
    private final IFrameContext iFrameContext;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    public IFrameIssueTab(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Optional<Condition> condition, final UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.iFrameContext = checkNotNull(iFrameContext);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.condition = condition;
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request)
    {
        return ImmutableList.<IssueAction>of(new IFrameIssueAction(request, iFrameRenderer, iFrameContext));
    }

    public static class IFrameIssueAction implements IssueAction
    {
        private final GetActionsRequest request;
        private final IFrameRenderer iFrameRenderer;
        private final IFrameContext iFrameContext;

        public IFrameIssueAction(GetActionsRequest request, IFrameRenderer iFrameRenderer, IFrameContext iFrameContext)
        {
            this.request = request;
            this.iFrameRenderer = iFrameRenderer;
            this.iFrameContext = iFrameContext;
        }

        @Override
        public String getHtml()
        {
            StringWriter writer = new StringWriter();
            try
            {
                Map<String, String[]> extraParams =
                        ImmutableMap.of("ctx_issue_key", new String[] { request.issue().getKey() });
                String remoteUserName = getRemoteUserName();
                writer.write(iFrameRenderer.render(iFrameContext, "", extraParams, remoteUserName));
            }
            catch (IOException e)
            {
                log.error("Error rendering tab", e);
                throw new RuntimeException(e);
            }
            return writer.toString();
        }

        private String getRemoteUserName()
        {
            return request.remoteUser() != null ? request.remoteUser().getName() : null;
        }

        @Override
        public Date getTimePerformed()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayActionAllTab()
        {
            return false;
        }
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request)
    {
        return !condition.isPresent() || condition.get().shouldDisplay(createConditionContext(request));

    }
}
