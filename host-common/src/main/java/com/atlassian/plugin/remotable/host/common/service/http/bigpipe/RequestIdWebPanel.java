package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.web.model.WebPanel;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A web panel that inserts a meta tag into head to allow javascript to access the request id. The
 * request id is used by big pipe to retrieve any delayed content for the page.
 */
public final class RequestIdWebPanel implements WebPanel
{
    private final BigPipe bigPipe;

    public RequestIdWebPanel(BigPipe bigPipe)
    {
        this.bigPipe = checkNotNull(bigPipe);
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
            throw new RuntimeException("Should never happen", e);
        }
        return writer.toString();
    }

    @Override
    public void writeHtml(Writer writer, Map<String, Object> context) throws IOException
    {
        final String requestId = bigPipe.getRequestId();
        writer.write("<meta name=\"ap-request-id\" content=\"" + requestId + "\">");
        writer.close();
    }
}
