package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.DefaultImagePlaceholder;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

public class ImagePlaceholderRemoteMacro extends RemoteMacro implements EditorImagePlaceholder
{
    public static final String REMOTE_IMAGE_SERVLET = "plugins/servlet/remoteImage";

    private final String pluginKey;
    private final String macroKey;
    private final String imageUrl;
    private final Dimensions dimensions;
    private final boolean applyChrome;

    public ImagePlaceholderRemoteMacro(String pluginKey,
                                       String macroKey,
                                       String imageUrl,
                                       Dimensions dimensions,
                                       boolean applyChrome,
                                       XhtmlContent xhtmlUtils,
                                       BodyType bodyType,
                                       OutputType outputType,
                                       String remoteUrl,
                                       ApplicationLinkOperationsFactory.LinkOperations linkOps,
                                       MacroContentManager macroContentManager)
    {
        super(xhtmlUtils, bodyType, outputType, remoteUrl, linkOps, macroContentManager);
        this.pluginKey = pluginKey;
        this.macroKey = macroKey;
        this.imageUrl = imageUrl;
        this.dimensions = dimensions;
        this.applyChrome = applyChrome;
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
}
