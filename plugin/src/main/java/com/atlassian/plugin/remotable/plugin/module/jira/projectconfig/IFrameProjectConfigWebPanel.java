package com.atlassian.plugin.remotable.plugin.module.jira.projectconfig;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.model.WebPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Web panel that display an iframe.
 */
public class IFrameProjectConfigWebPanel implements WebPanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameProjectConfigWebPanel.class);
    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;

    public IFrameProjectConfigWebPanel(final IFrameRenderer iFrameRenderer, final IFrameContext iFrameContext)
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
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
        try
        {
            User user = (User) context.get("user");

            String remoteUser = user != null ? user.getName() : null;
            writer.write(iFrameRenderer.render(iFrameContext, remoteUser));
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
