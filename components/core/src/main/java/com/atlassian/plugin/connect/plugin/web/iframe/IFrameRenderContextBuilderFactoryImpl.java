package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.UserPreferencesRetriever;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class IFrameRenderContextBuilderFactoryImpl implements IFrameRenderContextBuilderFactory
{
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final UserPreferencesRetriever userPreferencesRetriever;

    @Autowired
    public IFrameRenderContextBuilderFactoryImpl(final RemotablePluginAccessorFactory pluginAccessorFactory,
            final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
            final UserPreferencesRetriever userPreferencesRetriever)
    {
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.userPreferencesRetriever = userPreferencesRetriever;
    }

    @Override
    public IFrameRenderContextBuilder builder()
    {
        return new IFrameRenderContextBuilderImpl(pluginAccessorFactory, userManager, hostApplicationInfo,
                userPreferencesRetriever);
    }

}
