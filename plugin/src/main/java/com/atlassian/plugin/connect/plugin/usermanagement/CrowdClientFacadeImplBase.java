package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import static com.atlassian.crowd.integration.Constants.PROPERTIES_FILE;

public abstract class CrowdClientFacadeImplBase implements CrowdClientFacade
{
    private final CrowdClientFactory crowdClientFactory;

    public CrowdClientFacadeImplBase(CrowdClientFactory crowdClientFactory)
    {
        this.crowdClientFactory = crowdClientFactory;
    }

    @Override
    public CrowdClient getCrowdClient()
    {
        return crowdClientFactory.newInstance(getClientProperties());
    }

    private ClientProperties getClientProperties()
    {
        ClientResourceLocator resourceLocator = new ClientResourceLocator(PROPERTIES_FILE);
        return ClientPropertiesImpl.newInstanceFromResourceLocator(resourceLocator);
    }

}
