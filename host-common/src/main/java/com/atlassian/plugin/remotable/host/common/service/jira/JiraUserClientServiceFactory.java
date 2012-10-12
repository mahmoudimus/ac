package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.plugin.remotable.api.service.jira.JiraUserClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraUserClientServiceFactory implements TypedServiceFactory<JiraUserClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public JiraUserClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public JiraUserClient getService(Bundle bundle)
    {
        return new P3JiraUserClient(hostHttpClientServiceFactory.getService(bundle));
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
