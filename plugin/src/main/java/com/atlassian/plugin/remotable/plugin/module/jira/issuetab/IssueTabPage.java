package com.atlassian.plugin.remotable.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.*;
import com.atlassian.plugin.remotable.plugin.module.ContainingRemoteCondition;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;

/**
 * An issue tab that displays an iframe but isn't included in the all tab
 */
public class IssueTabPage extends AbstractIssueTabPanel2
{
    private static final Logger log = LoggerFactory.getLogger(IssueTabPage.class);
    private final IFrameRendererImpl iFrameRenderer;
    private final Condition condition;
    private final IFrameContext iFrameContext;

    public IssueTabPage(IFrameContext iFrameContext, IFrameRendererImpl iFrameRenderer,
            Condition condition)
    {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
        this.condition = condition;
    }

    @Override
    public ShowPanelReply showPanel(ShowPanelRequest request)
    {
        Map<String,Object> context = newHashMap();
        context.put("helper", singletonMap("project", request.issue().getProjectObject()));
        context.put("issue", request.issue());
        return ShowPanelReply.create(condition != null ? condition.shouldDisplay(context) : true);
    }

    @Override
    public GetActionsReply getActions(GetActionsRequest request)
    {
        return GetActionsReply.create(new IFrameIssueAction(request));
    }

    public class IFrameIssueAction implements IssueAction
    {
        private final GetActionsRequest request;

        public IFrameIssueAction(GetActionsRequest request)
        {
            this.request = request;
        }

        @Override
        public String getHtml()
        {
            StringWriter writer = new StringWriter();
            try
            {
                Map<String,String[]> extraParams = newHashMap();
                extraParams.put("ctx_issue_key", new String[]{request.issue().getKey()});
                String remoteUser = request.isAnonymous() ? null : request.remoteUser().getName();
                String iframe = iFrameRenderer.render(iFrameContext, "", extraParams,
                        remoteUser);
                if (condition != null && condition instanceof ContainingRemoteCondition)
                {
                    iframe = "<div>" + iframe + "</div>";
                }
                writer.write(iframe);
            }
            catch (PermissionDeniedException ex)
            {
                writer.write("Unauthorized to view this tab");
                log.warn("Unauthorized view of tab");
            }
            catch (IOException e)
            {
                writer.write("Unable to render tab: " + e.getMessage());
                log.error("Error rendering tab", e);
            }
            return writer.toString();
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
}
