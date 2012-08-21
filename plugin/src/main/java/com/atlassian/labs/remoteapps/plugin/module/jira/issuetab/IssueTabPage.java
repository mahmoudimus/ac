package com.atlassian.labs.remoteapps.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.*;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * An issue tab that displays an iframe but isn't included in the all tab
 */
public class IssueTabPage extends AbstractIssueTabPanel2
{
    private static final Logger log = LoggerFactory.getLogger(IssueTabPage.class);
    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;

    public IssueTabPage(IFrameContext iFrameContext, IFrameRenderer iFrameRenderer)
    {
        this.iFrameContext = iFrameContext;
        this.iFrameRenderer = iFrameRenderer;
    }

    @Override
    public ShowPanelReply showPanel(ShowPanelRequest request)
    {
        return ShowPanelReply.create(true);
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
                writer.write(iFrameRenderer.render(iFrameContext, "", extraParams,
                        remoteUser));
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
