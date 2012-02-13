package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.DefaultImagePlaceholder;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.thumbnail.Dimensions;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Wrapper to give a macro an image placeholder
 */
public class ImagePlaceholderMacroWrapper implements EditorImagePlaceholder, RemoteMacro
{
    private final RemoteMacro delegate;
    public static final String REMOTE_IMAGE_SERVLET = "plugins/servlet/remoteImage";

    private final String pluginKey;
    private final String macroKey;
    private final String imageUrl;
    private final Dimensions dimensions;
    private final boolean applyChrome;

    public ImagePlaceholderMacroWrapper(RemoteMacro delegate, boolean applyChrome,
            Dimensions dimensions,
            String imageUrl, String macroKey, String pluginKey)
    {
        this.delegate = delegate;
        this.applyChrome = applyChrome;
        this.dimensions = dimensions;
        this.imageUrl = imageUrl;
        this.macroKey = macroKey;
        this.pluginKey = pluginKey;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> parameters, ConversionContext context)
    {
        UriBuilder builder = UriBuilder.fromPath(REMOTE_IMAGE_SERVLET);
        builder.queryParam("pluginKey", pluginKey);
        builder.queryParam("macroKey", macroKey);
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        builder.queryParam("spaceKey", context.getSpaceKey());
        builder.queryParam("pageId", context.getEntity().getId());
        URI servletUri = builder.build();

        return new DefaultImagePlaceholder(servletUri.toString(), dimensions, applyChrome);
    }

    @Override
    public OutputType getOutputType()
    {
        return delegate.getOutputType();
    }

    @Override
    public String execute(Map<String, String> parameters, String body,
            ConversionContext context) throws MacroExecutionException
    {
        return delegate.execute(parameters, body, context);
    }

    @Override
    public BodyType getBodyType()
    {
        return delegate.getBodyType();
    }

    @Override
    public URI getBaseUrl()
    {
        return delegate.getBaseUrl();
    }
}
