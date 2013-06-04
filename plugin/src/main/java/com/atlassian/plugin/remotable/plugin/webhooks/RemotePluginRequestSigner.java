package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.fugue.Iterables;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.remotable.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.google.common.base.Predicate;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Signs outgoing webhooks with oauth credentials
 */
public class RemotePluginRequestSigner implements RequestSigner
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final PluginAccessor pluginAccessor;

    public RemotePluginRequestSigner(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory, PluginAccessor pluginAccessor)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public void sign(String pluginKey, Request request)
    {
        if (canSign(pluginKey))
        {
            final String authValue = remotablePluginAccessorFactory.get(pluginKey).getAuthorizationGenerator().generate("POST",
                    request.getUri(), Collections.<String, List<String>>emptyMap());

            if (authValue != null)
            {
                request.setHeader("Authorization", authValue);
            }
        }
    }

    public boolean canSign(final String pluginKey)
    {
        return !Iterables.findFirst(pluginAccessor.getPlugin(pluginKey).getModuleDescriptors(), new Predicate<ModuleDescriptor<?>>()
        {
            @Override
            public boolean apply(@Nullable ModuleDescriptor<?> input)
            {
                return input instanceof RemotePluginContainerModuleDescriptor;
            }
        }).isEmpty();
    }
}
