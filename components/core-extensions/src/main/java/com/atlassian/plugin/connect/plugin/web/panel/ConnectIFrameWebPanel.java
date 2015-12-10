package com.atlassian.plugin.connect.plugin.web.panel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.web.model.WebPanel;

/**
 *
 */
public class ConnectIFrameWebPanel implements WebPanel
{
    private final IFrameRenderStrategy renderStrategy;
    private final ModuleContextFilter moduleContextFilter;
    private final PluggableParametersExtractor moduleContextExtractor;

    public ConnectIFrameWebPanel(IFrameRenderStrategy renderStrategy, ModuleContextFilter moduleContextFilter,
            PluggableParametersExtractor moduleContextExtractor)
    {
        this.renderStrategy = renderStrategy;
        this.moduleContextFilter = moduleContextFilter;
        this.moduleContextExtractor = moduleContextExtractor;
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
        if (renderStrategy.shouldShow(context))
        {
            ModuleContextParameters unfilteredContext = moduleContextExtractor.extractParameters(context);
            ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);
            renderStrategy.render(filteredContext, writer, Optional.empty());
        }
        else
        {
            renderStrategy.renderAccessDenied(writer);
        }
    }

    @Override
    public String getHtml(final Map<String, Object> context)
    {
        // just delegate to writeHtml
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            writeHtml(new OutputStreamWriter(out), context);
        }
        catch (IOException e)
        {
            // no I/O, so no IOException.. right?
            throw new IllegalStateException(e);
        }
        return out.toString();
    }

}
