package com.atlassian.labs.remoteapps.spi.module;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.labs.remoteapps.plugin.module.IFrameRenderer;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import com.atlassian.plugin.web.model.WebPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Maps.newHashMap;

/**
 * View issue panel that loads its contents from an iframe
 */
public class IFrameViewIssuePanel implements WebPanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameViewIssuePanel.class);
    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;

    public IFrameViewIssuePanel(final IFrameRenderer iFrameRenderer, IFrameContext iFrameContext)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.iFrameContext = iFrameContext;
    }

    @Override
    public String getHtml(Map<String, Object> context)
    {

        StringWriter writer = new StringWriter();
        try
        {
            writeHtml(writer, context);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    @Override
    public void writeHtml(Writer writer, Map<String, Object> context) throws IOException
    {
        try
        {
            User user = (User) context.get("user");
            String remoteUser = user != null ? user.getName() : null;
            Map<String,String[]> params = newHashMap();
            params.put("issue_id", new String[]{context.containsKey("issue") ? ((Issue)context.get("issue")).getKey() : ""});
            params.put("project_id", new String[]{context.containsKey("project") ? ((Project)context.get("project")).getKey() : ""});

            writer.write(iFrameRenderer.render(iFrameContext, "", params, remoteUser));
        }
        catch (PermissionDeniedException ex)
        {
            writer.write("Unauthorized to view this panel");
            log.warn("Unauthorized view of panel");
        }
        catch (IOException e)
        {
            writer.write("Unable to render panel: " + e.getMessage());
            log.error("Error rendering panel", e);
        }
    }

}
