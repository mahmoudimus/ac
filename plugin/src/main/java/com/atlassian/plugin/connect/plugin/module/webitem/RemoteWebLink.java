package com.atlassian.plugin.connect.plugin.module.webitem;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.AbstractWebItem;
import com.atlassian.plugin.web.model.WebLink;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.addon;

/**
 * Link which points to the dialog, inline-dialog or general page coming from the add-on.
 */
public class RemoteWebLink extends AbstractWebItem implements WebLink
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final WebFragmentModuleContextExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final String url;
    private final String pluginKey;
    private final String moduleKey;
    private final boolean absolute;
    private final AddOnUrlContext addOnUrlContext;
    private final boolean isDialog;

    public RemoteWebLink(WebFragmentModuleDescriptor webFragmentModuleDescriptor, WebFragmentHelper webFragmentHelper,
                         IFrameUriBuilderFactory iFrameUriBuilderFactory, UrlVariableSubstitutor urlVariableSubstitutor,
                         WebFragmentModuleContextExtractor webFragmentModuleContextExtractor, ModuleContextFilter moduleContextFilter,
                         String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog)
    {
        super(webFragmentHelper, null, webFragmentModuleDescriptor);
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
        this.moduleContextFilter = moduleContextFilter;
        this.url = url;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.absolute = absolute;
        this.addOnUrlContext = addOnUrlContext;
        this.isDialog = isDialog;
    }

    @Override
    public String getRenderedUrl(final Map<String, Object> context)
    {
        ModuleContextParameters moduleParams = webFragmentModuleContextExtractor.extractParameters(context);
        moduleParams = moduleContextFilter.filter(moduleParams);

        return iFrameUriBuilderFactory.builder()
                                      .addOn(pluginKey)
                                      .namespace(moduleKey)
                                      .urlTemplate(url)
                                      .context(moduleParams)
                                      .sign(false)
                                      .includeStandardParams(false) // don't sign or pass context to absolute URLs
                                      .build();
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
            ModuleContextParameters moduleContext = webFragmentModuleContextExtractor.extractParameters(context);
            moduleContext = moduleContextFilter.filter(moduleContext);

            if (addOnUrlContext == addon)
            {
                return iFrameUriBuilderFactory.builder()
                                              .addOn(pluginKey)
                                              .namespace(moduleKey)
                                              .urlTemplate(url)
                                              .context(moduleContext)
                                              .dialog(isDialog)
                                              .build();
            }
            else
            {
                return req.getContextPath() + urlVariableSubstitutor.replace(url, moduleContext);
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
        return moduleKey;
    }
}
