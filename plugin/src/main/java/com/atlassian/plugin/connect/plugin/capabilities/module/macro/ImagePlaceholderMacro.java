package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.*;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.base.Preconditions;

public class ImagePlaceholderMacro implements Macro, EditorImagePlaceholder
{
    private final Macro delegate;
    private final Uri imageUri;
    private final Dimensions dimensions;
    private final boolean applyChrome;

    public ImagePlaceholderMacro(Macro delegate, Uri imageUri, Dimensions dimensions, boolean applyChrome)
    {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.imageUri = imageUri;
        this.dimensions = dimensions;
        this.applyChrome = applyChrome;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> parameters, ConversionContext context)
    {
        UriBuilder uriBuilder = new UriBuilder(imageUri);
        uriBuilder.addQueryParameters(parameters);
        return new DefaultImagePlaceholder(uriBuilder.toString(), dimensions, applyChrome);
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
