package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.httpclient.api.Request;
import com.atlassian.plugin.remotable.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.webhooks.spi.plugin.RequestSigner;

import java.util.Collections;
import java.util.List;

/**
 * Signs outgoing webhooks with oauth credentials
 */
public class RemotePluginRequestSigner implements RequestSigner
{
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;

    public RemotePluginRequestSigner(DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    @Override
    public void sign(String s, Request request)
    {

        String authValue = remotablePluginAccessorFactory.get(s).getAuthorizationGenerator().generate("POST",
                request.getUri(), Collections.<String, List<String>>emptyMap());

        if (authValue != null)
        {
            request.setHeader("Authorization", authValue);
        }
    }
}
