package com.atlassian.labs.remoteapps.host.common.service.jira;

import com.atlassian.jira.rest.client.p3.JiraUserClient;
import com.atlassian.jira.rest.client.p3.internal.P3JiraUserClient;
import com.atlassian.labs.remoteapps.host.common.service.TypedServiceFactory;
import com.atlassian.labs.remoteapps.host.common.service.http.HostHttpClientServiceFactory;
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
