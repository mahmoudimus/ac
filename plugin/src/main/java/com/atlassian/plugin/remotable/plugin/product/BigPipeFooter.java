package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.ConsumableBigPipe;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.String.format;

public final class BigPipeFooter implements WebPanel
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String FOOTER = "<script>" +
            "(function(){" +
            // is this really necessary since RC calls hide as well?
            "_AP.require(['condition/remote'], function(remoteCondition) { remoteCondition.hide(); });" +
            "_AP.require(['bigpipe/bigpipe'], function(bigPipe) { bigPipe.start({requestId:'%s',ready: %s}); });" +
            "}())" +
            "</script>\n";

    private final BigPipeManager bigPipeManager;

    public BigPipeFooter(BigPipeManager bigPipeManager)
    {
        this.bigPipeManager = checkNotNull(bigPipeManager);
    }

    public String getHtml(Map<String, Object> context)
    {
        final Writer writer = new StringWriter();
        try
        {
            writeHtml(writer, context);
            return writer.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Should never ever happen, we're using a StringWriter!", e);
        }
        finally
        {
            closeQuietly(writer);
        }
    }

    @Override
    public void writeHtml(Writer writer, Map<String, Object> context) throws IOException
    {
        writer.write(getHtml());
    }

    private String getHtml()
    {
        return bigPipeManager.getConsumableBigPipe().fold(
                new Supplier<String>()
                {
                    @Override
                    public String get()
                    {
                        logger.debug("Did NOT find ANY big pipe content, no footer required.");
                        return "";
                    }
                },
                new Function<ConsumableBigPipe, String>()
                {
                    @Override
                    public String apply(ConsumableBigPipe input)
                    {
                        final String requestId = input.getRequestId();
                        final String footer = format(FOOTER, requestId, input.consumeContent());
                        logger.debug("Found big pipe content for request {}, generated the following footer: {}", requestId, footer);
                        return footer;
                    }
                }
        );
    }
}
