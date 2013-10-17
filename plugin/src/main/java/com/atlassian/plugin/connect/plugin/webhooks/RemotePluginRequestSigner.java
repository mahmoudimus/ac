package com.atlassian.plugin.connect.plugin.webhooks;

import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.webhooks.spi.plugin.RequestSigner;
import com.google.common.base.Predicate;

import java.net.URI;
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
    public void sign(URI uri, String pluginKey, Request.Builder request)
    {
        if (canSign(pluginKey))
        {
            final Option<String> authValue = getAuthHeader(uri, pluginKey);
            if (authValue.isDefined())
            {
                request.setHeader("Authorization", authValue.get());
            }
        }
    }

    private Option<String> getAuthHeader(URI uri, String pluginKey)
    {
        return getAuthorizationGenerator(pluginKey).generate(HttpMethod.POST, uri, Collections.<String, List<String>>emptyMap());
    }

    private AuthorizationGenerator getAuthorizationGenerator(String pluginKey)
    {
        return remotablePluginAccessorFactory.get(pluginKey).getAuthorizationGenerator();
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
