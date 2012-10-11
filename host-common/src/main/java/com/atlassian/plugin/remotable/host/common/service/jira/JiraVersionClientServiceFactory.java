package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.jira.rest.client.p3.JiraVersionClient;
import com.atlassian.jira.rest.client.p3.internal.P3JiraVersionClient;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraVersionClientServiceFactory implements TypedServiceFactory<JiraVersionClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;

    public JiraVersionClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
    }

    @Override
    public JiraVersionClient getService(Bundle bundle)
    {
        return new P3JiraVersionClient(hostHttpClientServiceFactory.getService(bundle));
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
