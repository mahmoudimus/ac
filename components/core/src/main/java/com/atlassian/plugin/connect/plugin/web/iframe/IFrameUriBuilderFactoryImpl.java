package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilder;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.user.UserPreferencesRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IFrameUriBuilderFactoryImpl implements IFrameUriBuilderFactory
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private PluginRetrievalService pluginRetrievalService;

    @Autowired
    public IFrameUriBuilderFactoryImpl(final UrlVariableSubstitutor urlVariableSubstitutor,
                                       final RemotablePluginAccessorFactory pluginAccessorFactory,
                                       final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
                                       final LicenseRetriever licenseRetriever, final LocaleHelper localeHelper,
                                       final UserPreferencesRetriever userPreferencesRetriever,
                                       final PluginRetrievalService pluginRetrievalService)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = userPreferencesRetriever;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public IFrameUriBuilder builder()
    {
        return new IFrameUriBuilderImpl(urlVariableSubstitutor, pluginAccessorFactory, userManager, hostApplicationInfo,
                licenseRetriever, localeHelper, userPreferencesRetriever, pluginRetrievalService);
    }
}
