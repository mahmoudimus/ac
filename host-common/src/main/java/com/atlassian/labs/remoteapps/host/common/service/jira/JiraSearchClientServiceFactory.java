package com.atlassian.labs.remoteapps.host.common.service.jira;

import com.atlassian.jira.rest.client.p3.JiraSearchClient;
import com.atlassian.jira.rest.client.p3.internal.P3JiraSearchClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostHttpClientServiceFactory;
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
