package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.sal.api.timezone.TimeZoneManager;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IFrameRenderContextBuilderFactoryImpl implements IFrameRenderContextBuilderFactory {

    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final TimeZoneManager timeZoneManager;

    @Autowired
    public IFrameRenderContextBuilderFactoryImpl(RemotablePluginAccessorFactory pluginAccessorFactory,
                                                 UserManager userManager,
                                                 HostApplicationInfo hostApplicationInfo,
                                                 TimeZoneManager timeZoneManager) {
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    public IFrameRenderContextBuilder builder() {
        return new IFrameRenderContextBuilderImpl(pluginAccessorFactory, userManager, hostApplicationInfo,
                timeZoneManager);
    }
}
