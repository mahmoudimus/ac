package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.google.common.base.Preconditions;

import java.util.Map;

public class ImagePlaceHolderMacro implements Macro, EditorImagePlaceholder
{
    private final Macro delegate;
    private final ImagePlaceholder imagePlaceholder;

    public ImagePlaceHolderMacro(Macro delegate, ImagePlaceholder imagePlaceholder)
    {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.imagePlaceholder = imagePlaceholder;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> parameters, ConversionContext context)
    {
        return imagePlaceholder;
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
