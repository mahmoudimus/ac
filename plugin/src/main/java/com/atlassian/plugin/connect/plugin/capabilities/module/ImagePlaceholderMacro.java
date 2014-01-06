package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.DefaultImagePlaceholder;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.marketplace.client.util.UriBuilder;
import com.google.common.base.Preconditions;

import java.net.URI;
import java.util.Map;

public class ImagePlaceholderMacro implements Macro, EditorImagePlaceholder
{
    private final Macro delegate;
    private final URI imageUri;
    private final Dimensions dimensions;
    private final boolean applyChrome;

    public ImagePlaceholderMacro(Macro delegate, URI imageUri, Dimensions dimensions, boolean applyChrome)
    {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.imageUri = imageUri;
        this.dimensions = dimensions;
        this.applyChrome = applyChrome;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> parameters, ConversionContext context)
    {
        UriBuilder uriBuilder = UriBuilder.fromUri(imageUri);
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            uriBuilder.queryParam(entry.getKey(), entry.getValue());
        }
        return new DefaultImagePlaceholder(uriBuilder.build().toString(), dimensions, applyChrome);
    }

    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
    {
        return delegate.execute(parameters, body, context);
    }

    @Override
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType()
    {
        return delegate.getOutputType();
    }
}
