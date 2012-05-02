package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.DefaultImagePlaceholder;
import com.atlassian.confluence.macro.EditorImagePlaceholder;
import com.atlassian.confluence.macro.ImagePlaceholder;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.labs.remoteapps.modules.util.redirect.RedirectServlet;

import java.net.URI;
import java.util.Map;

/**
 * Wrapper to give a macro an image placeholder
 */
public class ImagePlaceholderMacroWrapper implements EditorImagePlaceholder, RemoteMacro
{
    private final RemoteMacro delegate;

    private final String pluginKey;
    private final String macroKey;
    private final URI imageUrl;
    private final Dimensions dimensions;
    private final boolean applyChrome;

    public ImagePlaceholderMacroWrapper(RemoteMacro delegate, boolean applyChrome,
            Dimensions dimensions,
            URI imageUrl, String macroKey, String pluginKey)
    {
        this.delegate = delegate;
        this.applyChrome = applyChrome;
        this.dimensions = dimensions;
        this.imageUrl = imageUrl;
        this.macroKey = macroKey;
        this.pluginKey = pluginKey;
    }

    @Override
    public ImagePlaceholder getImagePlaceholder(Map<String, String> parameters, final ConversionContext context)
    {
        MacroInstance macroInstance = new MacroInstance(context,
                delegate.getRemoteMacroInfo().getUrl(),
                "",
                parameters,
                delegate.getRemoteMacroInfo().getApplicationLinkOperations());

        String uri = RedirectServlet.getRelativeOAuthRedirectUrl(pluginKey, imageUrl, macroInstance.getUrlParameters());

        return new DefaultImagePlaceholder(uri, dimensions, applyChrome);
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

    @Override
    public RemoteMacroInfo getRemoteMacroInfo()
    {
        return delegate.getRemoteMacroInfo();
    }
}
