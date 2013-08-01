package com.atlassian.plugin.remotable.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel2;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsReply;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelReply;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.module.jira.JiraTabConditionContext.createConditionContext;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An issue tab that displays an iframe but isn't included in the all tab
 */
public class IFrameIssueTabPage extends AbstractIssueTabPanel2
{
    private static final Logger log = LoggerFactory.getLogger(IFrameIssueTabPage.class);
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;
    private final IFrameContext iFrameContext;

    public IFrameIssueTabPage(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer, Condition condition)
    {
        this.iFrameContext = checkNotNull(iFrameContext);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.condition = checkNotNull(condition);
    }

    @Override
    public GetActionsReply getActions(GetActionsRequest request)
    {
        return GetActionsReply.create(new IFrameIssueAction(request, iFrameRenderer, iFrameContext));
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
    public ShowPanelReply showPanel(ShowPanelRequest request)
    {
        return ShowPanelReply.create(condition == null || condition.shouldDisplay(createConditionContext(request)));
    }
}
