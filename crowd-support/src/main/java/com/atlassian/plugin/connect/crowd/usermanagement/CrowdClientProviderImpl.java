package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.integration.Constants;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrowdClientProviderImpl implements CrowdClientProvider
{
    private final CrowdClientFactory crowdClientFactory;

    @Autowired
    public CrowdClientProviderImpl(CrowdClientFactory crowdClientFactory)
    {
        this.crowdClientFactory = crowdClientFactory;
    }

    @Override
    public CrowdClient getCrowdClient()
    {
        ClientResourceLocator resourceLocator = new ClientResourceLocator(Constants.PROPERTIES_FILE);
        return crowdClientFactory.newInstance(ClientPropertiesImpl.newInstanceFromResourceLocator(resourceLocator));
    }

}
