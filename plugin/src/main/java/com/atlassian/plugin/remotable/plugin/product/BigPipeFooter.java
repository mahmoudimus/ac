package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.ConsumableBigPipe;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.base.Function;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class BigPipeFooter implements WebPanel
{
    private final BigPipeManager bigPipeManager;

    public BigPipeFooter(BigPipeManager bigPipeManager)
    {
        this.bigPipeManager = checkNotNull(bigPipeManager);
    }

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
        String html = bigPipeManager.getConsumableBigPipe().map(new Function<ConsumableBigPipe, String>()
        {
            @Override
            public String apply(ConsumableBigPipe input)
            {
                String json = input.consumeContent();
                return
                    "<script>" +
                        "(function(){" +
                            "var AP=this._AP=this._AP||{};" +
                            // is this really necessary since RC calls hide as well?
                            "AP.RemoteConditions.hide();" +
                            "AP.BigPipe.start({requestId:'" + input.getRequestId() + "',ready:" + json + "});" +
                        "}())" +
                    "</script>\n";
            }
        }).getOrElse("");
        writer.write(html);
        writer.close();
    }
}
