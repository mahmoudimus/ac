package com.atlassian.labs.remoteapps.host.common.service.jira;

import com.atlassian.jira.rest.client.p3.JiraMetadataClient;
import com.atlassian.jira.rest.client.p3.internal.P3JiraMetadataClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraMetadataClientServiceFactory implements TypedServiceFactory<JiraMetadataClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public JiraMetadataClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public JiraMetadataClient getService(Bundle bundle)
    {
        return new P3JiraMetadataClient(hostHttpClientServiceFactory.getService(bundle));
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
