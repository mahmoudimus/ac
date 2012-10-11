package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.jira.rest.client.p3.JiraIssueClient;
import com.atlassian.jira.rest.client.p3.internal.P3JiraIssueClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 */
public class JiraIssueClientServiceFactory implements TypedServiceFactory<JiraIssueClient>
{
    private final HostHttpClientServiceFactory hostHttpClientServiceFactory;
    private final RequestContextServiceFactory requestContextServiceFactory;
    private final JiraMetadataClientServiceFactory jiraMetadataClientServiceFactory;

    public JiraIssueClientServiceFactory(
            HostHttpClientServiceFactory hostHttpClientServiceFactory,
            RequestContextServiceFactory requestContextServiceFactory,
            JiraMetadataClientServiceFactory jiraMetadataClientServiceFactory)
    {
        this.hostHttpClientServiceFactory = hostHttpClientServiceFactory;
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.jiraMetadataClientServiceFactory = jiraMetadataClientServiceFactory;
    }

    @Override
    public JiraIssueClient getService(Bundle bundle)
    {
        return new P3JiraIssueClient(hostHttpClientServiceFactory.getService(bundle),
                requestContextServiceFactory.getService(bundle),
                jiraMetadataClientServiceFactory.getService(bundle));
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
