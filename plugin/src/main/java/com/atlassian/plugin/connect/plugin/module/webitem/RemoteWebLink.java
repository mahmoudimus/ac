package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.InvalidAddonConfigurationException;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.HideousParameterContextThingy;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.AbstractWebItem;
import com.atlassian.plugin.web.model.WebLink;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext.addon;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Link which points to the dialog, inline-dialog or general page coming from the add-on.
 */
public class RemoteWebLink extends AbstractWebItem implements WebLink
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer urlParametersSerializer;
    private final RemotablePluginAccessor remotablePluginAccessor;
    private final String url;
    private final String id;
    private final boolean absolute;
    private final AddOnUrlContext addOnUrlContext;

    public RemoteWebLink(
            WebFragmentModuleDescriptor webFragmentModuleDescriptor,
            WebFragmentHelper webFragmentHelper,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ContextMapURLSerializer urlParametersSerializer,
            RemotablePluginAccessor remotablePluginAccessor,
            String url,
            String id,
            boolean absolute, AddOnUrlContext addOnUrlContext)
    {
        super(webFragmentHelper, null, webFragmentModuleDescriptor);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.urlParametersSerializer = urlParametersSerializer;
        this.remotablePluginAccessor = checkNotNull(remotablePluginAccessor);
        this.url = url;
        this.id = id;
        this.absolute = absolute; // TODO: we could add absolute as another literal in the AddOnUrlContext enum
        this.addOnUrlContext = addOnUrlContext;
    }

    @Override
    public String getRenderedUrl(final Map<String, Object> context)
    {
        final Map<String, Object> extractedWebPanelParameters = urlParametersSerializer.getExtractedWebPanelParameters(context);
        return urlVariableSubstitutor.replace(url, extractedWebPanelParameters);
    }

    @Override
    public String getDisplayableUrl(final HttpServletRequest req, final Map<String, Object> context)
    {
        if (absolute)
        {
            return getRenderedUrl(context);
        }
        else
        {
            String renderedUrl = getRenderedUrl(context);
            if (addOnUrlContext == addon)
            {
                try
                {
                    return remotablePluginAccessor.signGetUrl(new URI(renderedUrl), HideousParameterContextThingy.transformToPathForm(context));
                }
                catch (URISyntaxException e)
                {
                    throw new InvalidAddonConfigurationException("invalid addon url " + url, e);
                }
            }
            else
            {
                return req.getContextPath() + renderedUrl;
            }
        }
    }

    @Override
    public boolean hasAccessKey()
    {
        return false;
    }

    @Override
    public String getAccessKey(final Map<String, Object> context)
    {
        return null;
    }

    @Override
    public String getId()
    {
        return id;
    }
}
