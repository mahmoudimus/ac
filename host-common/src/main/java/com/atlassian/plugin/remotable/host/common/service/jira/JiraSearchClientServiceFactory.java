package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.plugin.remotable.api.service.jira.JiraSearchClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraSearchClientServiceFactory implements TypedServiceFactory<JiraSearchClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public JiraSearchClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public JiraSearchClient getService(Bundle bundle)
    {
        return new P3JiraSearchClient(hostHttpClientServiceFactory.getService(bundle));
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
