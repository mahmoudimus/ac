package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.ConnectAddonUriBuilder;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.timezone.TimeZoneManager;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;

@Component
public class ConnectUriFactoryImpl implements ConnectUriFactory
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final TimeZoneManager timeZoneManager;
    private PluginRetrievalService pluginRetrievalService;

    @Autowired
    public ConnectUriFactoryImpl(final UrlVariableSubstitutor urlVariableSubstitutor,
            final RemotablePluginAccessorFactory pluginAccessorFactory,
            final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
            final LicenseRetriever licenseRetriever, final LocaleHelper localeHelper,
            final TimeZoneManager timeZoneManager,
            final PluginRetrievalService pluginRetrievalService)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.timeZoneManager = timeZoneManager;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public ConnectAddonUriBuilder createConnectAddonUriBuilder()
    {
        return new ConnectAddonUriBuilderImpl(urlVariableSubstitutor, pluginAccessorFactory, userManager, hostApplicationInfo,
                licenseRetriever, localeHelper, timeZoneManager, pluginRetrievalService);
    }

    @Override
    public String createConnectIFrameServletUri(final String addOnKey, final String moduleKey, final ModuleContextParameters moduleContext)
    {
        return urlVariableSubstitutor.append(ConnectIFrameServletPath.forModule(addOnKey, moduleKey), moduleContext);
    }

    @Override
    public String createRedirectServletUri(final String addOnKey, final String moduleKey, final ModuleContextParameters moduleContext)
    {
        String urlToRedirectServlet = UriBuilder.fromUri(hostApplicationInfo.getUrl()).path(hostApplicationInfo.getContextPath()).path(RedirectServletPath.forModule(addOnKey, moduleKey)).build().toString();
        return urlVariableSubstitutor.append(urlToRedirectServlet, moduleContext);
    }
}
