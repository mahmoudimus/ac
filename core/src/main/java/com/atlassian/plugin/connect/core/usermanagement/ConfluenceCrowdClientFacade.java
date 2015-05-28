package com.atlassian.plugin.connect.core.usermanagement;

import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ConfluenceCrowdClientFacade extends CrowdClientFacadeImplBase implements CrowdClientFacade
{
    @Autowired
    public ConfluenceCrowdClientFacade(CrowdClientFactory crowdClientFactory)
    {
        super(crowdClientFactory);
    }

    @Override
    public String getClientApplicationName()
    {
        return "confluence";
    }
}
