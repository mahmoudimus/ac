package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.AbstractWebItem;
import com.atlassian.plugin.web.model.WebLink;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Link which points to the dialog, inline-dialog or general page coming from the add-on.
 */
public class RemoteWebLink extends AbstractWebItem implements WebLink
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer urlParametersSerializer;
    private final String url;
    private final String key;
    private final boolean absolute;

    public RemoteWebLink(
            WebFragmentModuleDescriptor webFragmentModuleDescriptor,
            WebFragmentHelper webFragmentHelper,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ContextMapURLSerializer urlParametersSerializer,
            String url,
            String key,
            boolean absolute)
    {
        super(webFragmentHelper, null, webFragmentModuleDescriptor);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.urlParametersSerializer = urlParametersSerializer;
        this.url = url;
        this.key = key;
        this.absolute = absolute;
    }

    @Override
    public String getRenderedUrl(final Map<String, Object> context)
    {
        final Map<String, Object> extractedWebPanelParameters = urlParametersSerializer.getExtractedWebPanelParameters(context, "TODO: inject UserManager");
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
            return req.getContextPath() + renderedUrl;
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
        return key;
    }
}
