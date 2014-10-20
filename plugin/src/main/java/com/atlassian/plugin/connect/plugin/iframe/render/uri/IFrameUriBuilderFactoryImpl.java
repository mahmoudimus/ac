package com.atlassian.plugin.connect.plugin.iframe.render.uri;

import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.module.HostApplicationInfo;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserManager;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
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
    private final BundleContext bundleContext;

    @Autowired
    public IFrameUriBuilderFactoryImpl(final UrlVariableSubstitutor urlVariableSubstitutor,
                                       final RemotablePluginAccessorFactory pluginAccessorFactory,
                                       final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
                                       final LicenseRetriever licenseRetriever, final LocaleHelper localeHelper,
                                       final UserPreferencesRetriever userPreferencesRetriever,
                                       final BundleContext bundleContext)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = userPreferencesRetriever;
        this.bundleContext = bundleContext;
    }

    @Override
    public IFrameUriBuilder builder()
    {
        return new IFrameUriBuilderImpl(urlVariableSubstitutor, pluginAccessorFactory, userManager, hostApplicationInfo,
                licenseRetriever, localeHelper, userPreferencesRetriever, bundleContext);
    }
}
