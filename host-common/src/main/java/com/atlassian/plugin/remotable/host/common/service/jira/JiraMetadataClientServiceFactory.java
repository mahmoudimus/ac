package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.plugin.remotable.api.service.jira.JiraMetadataClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
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
