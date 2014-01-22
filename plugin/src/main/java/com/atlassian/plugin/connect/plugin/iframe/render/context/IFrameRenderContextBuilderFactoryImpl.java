package com.atlassian.plugin.connect.plugin.iframe.render.context;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.module.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
@Component
public class IFrameRenderContextBuilderFactoryImpl implements IFrameRenderContextBuilderFactory, InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(IFrameRenderContextBuilderFactoryImpl.class);

    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private final PluginRetrievalService pluginRetrievalService;
    private final WebResourceUrlProvider webResourceUrlProvider;

    private List<String> dialogScriptUrls;

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
    public void afterPropertiesSet() throws Exception
    {
        // eagerly determine the dialogScriptUrl
        dialogScriptUrls = newArrayList();
        ModuleDescriptor<?> dialogModuleDescriptor = pluginRetrievalService.getPlugin().getModuleDescriptor("dialog");
        for (ResourceDescriptor descriptor : dialogModuleDescriptor.getResourceDescriptors())
        {
            String src = webResourceUrlProvider.getStaticPluginResourceUrl(dialogModuleDescriptor, descriptor.getName(), UrlMode.AUTO);
            if (src.endsWith("js"))
            {
                dialogScriptUrls.add(src);
            }
        }
        if (dialogScriptUrls.isEmpty())
        {
            throw new IllegalStateException("Expected at least one JS resource URL for <web-resource key='dialog'>.");
        }
    }

    @Override
    public IFrameRenderContextBuilder builder()
    {
        return new IFrameRenderContextBuilderImpl(pluginAccessorFactory, userManager, hostApplicationInfo,
                userPreferencesRetriever, Preconditions.checkNotNull(dialogScriptUrls, "dialogScriptUrls"));
    }

}
