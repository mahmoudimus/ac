package com.atlassian.plugin.connect.core.iframe.render.context;

import com.atlassian.plugin.connect.core.module.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.user.UserPreferencesRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class IFrameRenderContextBuilderFactoryImpl implements IFrameRenderContextBuilderFactory
{
    private static final Logger log = LoggerFactory.getLogger(IFrameRenderContextBuilderFactoryImpl.class);

    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private final PluginRetrievalService pluginRetrievalService;
    private final WebResourceUrlProvider webResourceUrlProvider;


    @Autowired
    public IFrameRenderContextBuilderFactoryImpl(final RemotablePluginAccessorFactory pluginAccessorFactory,
            final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
            final UserPreferencesRetriever userPreferencesRetriever,
            final PluginRetrievalService pluginRetrievalService, WebResourceUrlProvider webResourceUrlProvider)
    {
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.userPreferencesRetriever = userPreferencesRetriever;
        this.pluginRetrievalService = pluginRetrievalService;
        this.webResourceUrlProvider = webResourceUrlProvider;
    }


    @Override
    public IFrameRenderContextBuilder builder()
    {
        return new IFrameRenderContextBuilderImpl(pluginAccessorFactory, userManager, hostApplicationInfo,
                userPreferencesRetriever);
    }

}
