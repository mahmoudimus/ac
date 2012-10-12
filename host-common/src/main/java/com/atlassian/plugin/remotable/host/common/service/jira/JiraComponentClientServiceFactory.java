package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.plugin.remotable.api.service.jira.JiraComponentClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraComponentClientServiceFactory implements TypedServiceFactory<JiraComponentClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public JiraComponentClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public JiraComponentClient getService(Bundle bundle)
    {
        return new P3JiraComponentClient(hostHttpClientServiceFactory.getService(bundle));
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
