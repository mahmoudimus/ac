package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.crowd.integration.Constants.PROPERTIES_FILE;

@Component
public class CrowdClientFacadeImpl implements CrowdClientFacade
{
    private final CrowdClientFactory crowdClientFactory;

    @Autowired
    public CrowdClientFacadeImpl(CrowdClientFactory crowdClientFactory)
    {
        this.crowdClientFactory = crowdClientFactory;
    }

    @Override
    public CrowdClient getCrowdClient()
    {
        return crowdClientFactory.newInstance(getClientProperties());
    }

    @Override
    public String getClientApplicationName()
    {
        return getClientProperties().getApplicationName();
    }

    private ClientProperties getClientProperties()
    {
        ClientResourceLocator resourceLocator = new ClientResourceLocator(PROPERTIES_FILE);
        return ClientPropertiesImpl.newInstanceFromResourceLocator(resourceLocator);
    }

}
