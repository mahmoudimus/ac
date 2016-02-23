package com.atlassian.plugin.connect.api.web;

import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.descriptors.WebFragmentModuleDescriptor;
import com.atlassian.plugin.web.model.AbstractWebItem;
import com.atlassian.plugin.web.model.WebLink;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AddonUrlContext.addon;
import static com.atlassian.plugin.connect.modules.beans.AddonUrlContext.page;

/**
 * Link which points to the dialog, inline-dialog or general page coming from the add-on.
 */
public class RemoteWebLink extends AbstractWebItem implements WebLink {
    private final ConnectUriFactory connectUriFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final PluggableParametersExtractor webFragmentModuleContextExtractor;
    private final ModuleContextFilter moduleContextFilter;
    private final String url;
    private final String pluginKey;
    private final String moduleKey;
    private final boolean absolute;
    private final AddonUrlContext addonUrlContext;
    private final boolean isDialog;

    public RemoteWebLink(WebFragmentModuleDescriptor webFragmentModuleDescriptor, WebFragmentHelper webFragmentHelper,
                         ConnectUriFactory connectUriFactory, UrlVariableSubstitutor urlVariableSubstitutor,
                         PluggableParametersExtractor webFragmentModuleContextExtractor, ModuleContextFilter moduleContextFilter,
                         String url, String pluginKey, String moduleKey, boolean absolute, AddonUrlContext addonUrlContext, boolean isDialog) {
        super(webFragmentHelper, null, webFragmentModuleDescriptor);
        this.connectUriFactory = connectUriFactory;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.webFragmentModuleContextExtractor = webFragmentModuleContextExtractor;
        this.moduleContextFilter = moduleContextFilter;
        this.url = url;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.absolute = absolute;
        this.addonUrlContext = addonUrlContext;
        this.isDialog = isDialog;
    }

    @Override
    public String getRenderedUrl(final Map<String, Object> context) {
        ModuleContextParameters moduleParams = webFragmentModuleContextExtractor.extractParameters(context);
        moduleParams = moduleContextFilter.filter(moduleParams);

        return connectUriFactory.createConnectAddonUriBuilder()
                .addon(pluginKey)
                .namespace(moduleKey)
                .urlTemplate(url)
                .context(moduleParams)
                .sign(false)
                .includeStandardParams(false) // don't sign or pass non-explicitly requested parameters to absolute URLs
                .build();
    }

    @Override
    public String getDisplayableUrl(final HttpServletRequest req, final Map<String, Object> context) {
        if (absolute) {
            return getRenderedUrl(context);
        } else {
            ModuleContextParameters moduleContext = webFragmentModuleContextExtractor.extractParameters(context);
            moduleContext = moduleContextFilter.filter(moduleContext);

            if (addonUrlContext == addon) {
                if (isDialog) {
                    // Url to the the ConnectIFrameServlet does not need to have base url.
                    // The url is only used by JS to parse url params from it.
                    // Then JS compose new url to the ConnectIFrameServlet and do a request.
                    return connectUriFactory.createConnectIFrameServletUri(pluginKey, moduleKey, moduleContext);
                } else {
                    return connectUriFactory.createRedirectServletUri(pluginKey, moduleKey, moduleContext);
                }
            } else if (addonUrlContext == page) {
                return req.getContextPath() + urlVariableSubstitutor.append(url, moduleContext);
            } else // if (addonUrlContext == product)
            {
                return req.getContextPath() + urlVariableSubstitutor.replace(url, WebFragmentContext.from(moduleContext));
            }
        }
    }

    @Override
    public boolean hasAccessKey() {
        return false;
    }

    @Override
    public String getAccessKey(final Map<String, Object> context) {
        return null;
    }

    @Override
    public String getId() {
        return moduleKey;
    }
}
